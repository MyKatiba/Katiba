#!/usr/bin/env python3
"""
Constitution of Kenya PDF Parser

This script parses the Constitution of Kenya PDF and outputs structured YAML
that can be used by the Katiba app.

Requirements:
    pip install pymupdf pyyaml

Usage:
    python parse_constitution.py
"""

import re
import yaml
import sys
from pathlib import Path

try:
    import fitz  # PyMuPDF
except ImportError:
    print("Please install PyMuPDF: pip install pymupdf")
    sys.exit(1)


def extract_text_from_pdf(pdf_path: str) -> str:
    """Extract all text from the PDF."""
    doc = fitz.open(pdf_path)
    text = ""
    for page in doc:
        text += page.get_text()
    doc.close()
    return text


def parse_constitution(text: str) -> dict:
    """Parse the constitution text into structured data."""

    result = {
        "preamble": "",
        "chapters": []
    }

    # Roman numeral to number mapping
    roman_to_num = {
        "ONE": 1, "TWO": 2, "THREE": 3, "FOUR": 4, "FIVE": 5,
        "SIX": 6, "SEVEN": 7, "EIGHT": 8, "NINE": 9, "TEN": 10,
        "ELEVEN": 11, "TWELVE": 12, "THIRTEEN": 13, "FOURTEEN": 14,
        "FIFTEEN": 15, "SIXTEEN": 16, "SEVENTEEN": 17, "EIGHTEEN": 18
    }

    # Extract preamble
    preamble_match = re.search(r'PREAMBLE\s*(.*?)(?=CHAPTER)', text, re.DOTALL | re.IGNORECASE)
    if preamble_match:
        result["preamble"] = clean_text(preamble_match.group(1))

    # Split into chapters
    chapter_pattern = r'CHAPTER\s+(\w+)\s*[-–—]?\s*([A-Z][A-Z\s,]+?)(?=\n)'
    chapter_matches = list(re.finditer(chapter_pattern, text, re.IGNORECASE))

    for i, match in enumerate(chapter_matches):
        chapter_num_text = match.group(1).strip().upper()
        chapter_num = roman_to_num.get(chapter_num_text, i + 1)
        chapter_title = clean_text(match.group(2))

        # Get chapter content (until next chapter or end)
        start = match.end()
        end = chapter_matches[i + 1].start() if i + 1 < len(chapter_matches) else len(text)
        chapter_text = text[start:end]

        # Parse articles in this chapter
        articles = parse_articles(chapter_text)

        result["chapters"].append({
            "number": chapter_num,
            "title": chapter_title,
            "articles": articles
        })

    return result


def parse_articles(chapter_text: str) -> list:
    """Parse articles from chapter text."""
    articles = []

    # Pattern for article headers: number followed by period and title
    article_pattern = r'^(\d+)\.\s+([A-Z][^0-9\n]+?)(?=\n)'
    article_matches = list(re.finditer(article_pattern, chapter_text, re.MULTILINE))

    for i, match in enumerate(article_matches):
        article_num = int(match.group(1))
        article_title = clean_text(match.group(2))

        # Get article content
        start = match.end()
        end = article_matches[i + 1].start() if i + 1 < len(article_matches) else len(chapter_text)
        article_text = chapter_text[start:end]

        # Parse clauses
        clauses = parse_clauses(article_text)

        if clauses:  # Only add if we found clauses
            articles.append({
                "number": article_num,
                "title": article_title,
                "clauses": clauses
            })

    return articles


def parse_clauses(article_text: str) -> list:
    """Parse clauses from article text."""
    clauses = []

    # Pattern for numbered clauses: (1), (2), etc.
    clause_pattern = r'\((\d+)\)\s*'
    parts = re.split(clause_pattern, article_text)

    # parts will be: [before first clause, num1, text1, num2, text2, ...]
    if len(parts) > 1:
        for i in range(1, len(parts), 2):
            if i + 1 < len(parts):
                clause_num = parts[i]
                clause_text = parts[i + 1].strip()

                # Parse sub-clauses (a), (b), etc.
                sub_clauses = parse_sub_clauses(clause_text)

                # Clean the main clause text (remove sub-clause content)
                main_text = re.split(r'\([a-z]\)', clause_text)[0].strip()

                clauses.append({
                    "number": clause_num,
                    "text": clean_text(main_text),
                    "subClauses": sub_clauses
                })
    else:
        # No numbered clauses, treat the whole text as one clause
        cleaned = clean_text(article_text)
        if cleaned:
            clauses.append({
                "number": "",
                "text": cleaned,
                "subClauses": []
            })

    return clauses


def parse_sub_clauses(clause_text: str) -> list:
    """Parse sub-clauses (a), (b), etc. from clause text."""
    sub_clauses = []

    sub_pattern = r'\(([a-z])\)\s*'
    parts = re.split(sub_pattern, clause_text)

    if len(parts) > 1:
        for i in range(1, len(parts), 2):
            if i + 1 < len(parts):
                label = parts[i]
                text = clean_text(parts[i + 1])
                if text:
                    sub_clauses.append({
                        "label": label,
                        "text": text
                    })

    return sub_clauses


def clean_text(text: str) -> str:
    """Clean and normalize text."""
    if not text:
        return ""
    # Replace multiple whitespace with single space
    text = re.sub(r'\s+', ' ', text)
    # Remove leading/trailing whitespace
    text = text.strip()
    # Remove trailing periods if followed by nothing
    text = re.sub(r'\.$', '', text)
    return text


def main():
    # Paths
    script_dir = Path(__file__).parent
    project_root = script_dir.parent
    pdf_path = project_root / "docs" / "Constitution_of_Kenya.pdf"
    output_path = project_root / "composeApp" / "src" / "commonMain" / "composeResources" / "files" / "constitution.yaml"
    raw_text_path = project_root / "docs" / "constitution_raw_text.txt"

    print("=" * 60)
    print("Constitution of Kenya PDF Parser")
    print("=" * 60)
    print(f"PDF Input: {pdf_path}")
    print(f"YAML Output: {output_path}")
    print()

    if not pdf_path.exists():
        print(f"ERROR: PDF file not found at {pdf_path}")
        sys.exit(1)

    # Extract text
    print("Extracting text from PDF...")
    text = extract_text_from_pdf(str(pdf_path))

    # Save raw text for inspection
    raw_text_path.write_text(text, encoding='utf-8')
    print(f"Raw text saved to: {raw_text_path}")
    print(f"Raw text length: {len(text)} characters")
    print()

    # Parse
    print("Parsing constitution structure...")
    constitution = parse_constitution(text)

    # Summary
    print("=" * 60)
    print("Parsing Summary")
    print("=" * 60)
    print(f"Preamble: {constitution['preamble'][:100]}..." if constitution['preamble'] else "Preamble: (not found)")
    print(f"Chapters found: {len(constitution['chapters'])}")

    for chapter in constitution['chapters']:
        print(f"  Chapter {chapter['number']}: {chapter['title']}")
        print(f"    Articles: {len(chapter['articles'])}")
        for article in chapter['articles'][:3]:
            print(f"      Article {article['number']}: {article['title']}")
            print(f"        Clauses: {len(article['clauses'])}")
        if len(chapter['articles']) > 3:
            print(f"      ... and {len(chapter['articles']) - 3} more articles")
    print()

    # Save YAML
    print("Saving YAML...")
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with open(output_path, 'w', encoding='utf-8') as f:
        yaml.dump(constitution, f, default_flow_style=False, allow_unicode=True, sort_keys=False)

    print(f"YAML saved to: {output_path}")
    print(f"YAML size: {output_path.stat().st_size} bytes")
    print()
    print("=" * 60)
    print("SUCCESS!")
    print("=" * 60)


if __name__ == "__main__":
    main()

