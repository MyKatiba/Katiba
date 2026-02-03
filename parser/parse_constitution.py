#!/usr/bin/env python3
"""
Constitution of Kenya 2010 Parser

Parses The_Constitution_of_Kenya_2010.txt into structured JSON for the Katiba app.

Hierarchy: Chapters -> Parts -> Articles -> Clauses -> SubClauses -> MiniClauses

Usage:
    python parse_constitution.py
"""

import re
import json
from pathlib import Path
from typing import Optional


def read_constitution_text(file_path: Path) -> str:
    """Read the constitution text file."""
    with open(file_path, 'r', encoding='utf-8') as f:
        return f.read()


def clean_text(text: str) -> str:
    """Clean and normalize text."""
    if not text:
        return ""
    # Replace multiple whitespace with single space
    text = re.sub(r'\s+', ' ', text)
    # Remove page headers/footers
    text = re.sub(r'Constitution of Kenya, 2010\s*\d*', '', text)
    # Remove leading/trailing whitespace
    text = text.strip()
    return text


def parse_roman_numeral(s: str) -> int:
    """Convert lowercase roman numeral to integer."""
    roman_map = {'i': 1, 'v': 5, 'x': 10, 'l': 50, 'c': 100}
    result = 0
    prev = 0
    for char in reversed(s.lower()):
        val = roman_map.get(char, 0)
        if val < prev:
            result -= val
        else:
            result += val
        prev = val
    return result


def word_to_num(word: str) -> int:
    """Convert word number to integer (ONE -> 1, etc.)."""
    word_map = {
        "ONE": 1, "TWO": 2, "THREE": 3, "FOUR": 4, "FIVE": 5,
        "SIX": 6, "SEVEN": 7, "EIGHT": 8, "NINE": 9, "TEN": 10,
        "ELEVEN": 11, "TWELVE": 12, "THIRTEEN": 13, "FOURTEEN": 14,
        "FIFTEEN": 15, "SIXTEEN": 16, "SEVENTEEN": 17, "EIGHTEEN": 18
    }
    return word_map.get(word.upper(), 0)


def parse_mini_clauses(text: str) -> tuple[str, list]:
    """
    Parse mini-clauses (i), (ii), (iii) from text.
    Returns the main text and a list of mini-clauses.
    """
    mini_clauses = []
    
    # Pattern for roman numeral mini-clauses: (i), (ii), (iii), (iv), etc.
    mini_pattern = r'\(([ivxlc]+)\)\s*'
    parts = re.split(mini_pattern, text)
    
    if len(parts) > 1:
        main_text = parts[0].strip()
        for i in range(1, len(parts), 2):
            if i + 1 < len(parts):
                numeral = parts[i]
                content = clean_text(parts[i + 1])
                if content and re.match(r'^[ivxlc]+$', numeral):
                    mini_clauses.append({
                        "numeral": numeral,
                        "number": parse_roman_numeral(numeral),
                        "text": content
                    })
        return main_text, mini_clauses
    
    return text, []


def parse_sub_clauses(text: str) -> tuple[str, list]:
    """
    Parse sub-clauses (a), (b), (c) from text.
    Returns the main text and a list of sub-clauses with potential mini-clauses.
    """
    sub_clauses = []
    
    # Pattern for lettered sub-clauses: (a), (b), (c), etc.
    sub_pattern = r'\(([a-z])\)\s*'
    parts = re.split(sub_pattern, text)
    
    if len(parts) > 1:
        main_text = parts[0].strip()
        for i in range(1, len(parts), 2):
            if i + 1 < len(parts):
                label = parts[i]
                content = parts[i + 1].strip()
                
                # Check for mini-clauses within this sub-clause
                sub_text, mini_clauses = parse_mini_clauses(content)
                
                sub_clause = {
                    "label": label,
                    "text": clean_text(sub_text)
                }
                
                if mini_clauses:
                    sub_clause["miniClauses"] = mini_clauses
                
                sub_clauses.append(sub_clause)
        
        return main_text, sub_clauses
    
    return text, []


def parse_clauses(article_text: str) -> list:
    """
    Parse clauses (1), (2), (3) from article text.
    Returns a list of clauses with potential sub-clauses.
    """
    clauses = []
    
    # Pattern for numbered clauses: (1), (2), (3), etc.
    clause_pattern = r'\((\d+)\)\s*'
    parts = re.split(clause_pattern, article_text)
    
    if len(parts) > 1:
        for i in range(1, len(parts), 2):
            if i + 1 < len(parts):
                clause_num = parts[i]
                content = parts[i + 1].strip()
                
                # Parse sub-clauses
                clause_text, sub_clauses = parse_sub_clauses(content)
                
                clause = {
                    "number": int(clause_num),
                    "text": clean_text(clause_text)
                }
                
                if sub_clauses:
                    clause["subClauses"] = sub_clauses
                
                clauses.append(clause)
    else:
        # No numbered clauses - article has only text
        cleaned = clean_text(article_text)
        if cleaned:
            clauses.append({
                "number": 0,
                "text": cleaned,
                "isTextOnly": True
            })
    
    return clauses


def parse_articles(text: str, start_article: int = 1) -> list:
    """
    Parse articles from a section of text.
    Articles are identified by a number followed by a period and title.
    """
    articles = []
    
    # Pattern: Article number, title on line before, then content
    # Article headers appear as: "Title.\n    N. (1) content" or "Title.\n N. content"
    
    # Find article patterns: number at start of line or after whitespace
    # Format: "Title.\n     N. (1) text" or "N. (1) text"
    
    lines = text.split('\n')
    current_article = None
    current_title = ""
    current_content = []
    
    # Regex for article number at start: "    1. (1)" or "1. (1)" or just "1. "
    article_start_pattern = re.compile(r'^\s*(\d+)\.\s+(?:\(1\)\s*)?(.*)$')
    # Title line pattern: ends with period, no article number
    title_pattern = re.compile(r'^([A-Z][^.]*\.)\s*$')
    
    for line in lines:
        # Skip page markers
        if re.search(r'Constitution of Kenya, 2010', line):
            continue
        
        # Check if this is a new article start
        match = article_start_pattern.match(line)
        if match:
            # Save previous article
            if current_article is not None:
                article_content = '\n'.join(current_content)
                clauses = parse_clauses(article_content)
                articles.append({
                    "number": current_article,
                    "title": clean_text(current_title),
                    "clauses": clauses
                })
            
            current_article = int(match.group(1))
            remainder = match.group(2)
            current_content = [remainder] if remainder else []
            
            # Title was captured from previous lines
            continue
        
        # Check if this might be a title line (for next article)
        title_match = title_pattern.match(line.strip())
        if title_match and not line.strip().startswith('('):
            # This could be a title for the next article
            current_title = title_match.group(1).rstrip('.')
            continue
        
        # Otherwise, accumulate content
        if current_article is not None:
            current_content.append(line)
    
    # Save last article
    if current_article is not None:
        article_content = '\n'.join(current_content)
        clauses = parse_clauses(article_content)
        articles.append({
            "number": current_article,
            "title": clean_text(current_title),
            "clauses": clauses
        })
    
    return articles


def parse_part(text: str, part_num: int, part_title: str) -> dict:
    """Parse a Part section within a Chapter."""
    articles = parse_articles(text)
    return {
        "number": part_num,
        "title": clean_text(part_title),
        "articles": articles
    }


def extract_parts_from_chapter(chapter_text: str) -> tuple[list, list]:
    """
    Extract Parts from chapter text.
    Returns (parts_list, articles_outside_parts)
    """
    parts = []
    
    # Pattern for parts: "PART 1-TITLE" or "PART 1 - TITLE"
    part_pattern = re.compile(r'PART\s+(\d+)\s*[-–—]\s*([A-Z][A-Z\s,]+)', re.IGNORECASE)
    part_matches = list(part_pattern.finditer(chapter_text))
    
    if not part_matches:
        # No parts found, articles are directly in chapter
        articles = parse_articles(chapter_text)
        return [], articles
    
    # Articles before first part
    pre_part_text = chapter_text[:part_matches[0].start()]
    articles_before = parse_articles(pre_part_text) if pre_part_text.strip() else []
    
    # Parse each part
    for i, match in enumerate(part_matches):
        part_num = int(match.group(1))
        part_title = match.group(2).strip()
        
        start = match.end()
        end = part_matches[i + 1].start() if i + 1 < len(part_matches) else len(chapter_text)
        part_text = chapter_text[start:end]
        
        part = parse_part(part_text, part_num, part_title)
        parts.append(part)
    
    return parts, articles_before


def parse_chapters(text: str) -> list:
    """Parse all chapters from the constitution text."""
    chapters = []
    
    # Find where schedules start and truncate text to only parse chapters
    schedule_start = re.search(r'SCHEDULES\s+FIRST\s+SCHEDULE', text, re.IGNORECASE)
    chapters_text = text[:schedule_start.start()] if schedule_start else text
    
    # Pattern for chapter headers
    chapter_pattern = re.compile(
        r'CHAPTER\s+(ONE|TWO|THREE|FOUR|FIVE|SIX|SEVEN|EIGHT|NINE|TEN|ELEVEN|TWELVE|THIRTEEN|FOURTEEN|FIFTEEN|SIXTEEN|SEVENTEEN|EIGHTEEN)\s*[-–—]\s*([A-Z][A-Z\s,]+?)(?=\r?\n)',
        re.IGNORECASE
    )
    
    chapter_matches = list(chapter_pattern.finditer(chapters_text))
    
    for i, match in enumerate(chapter_matches):
        chapter_word = match.group(1).upper()
        chapter_num = word_to_num(chapter_word)
        chapter_title = match.group(2).strip()
        
        # Get chapter content
        start = match.end()
        end = chapter_matches[i + 1].start() if i + 1 < len(chapter_matches) else len(chapters_text)
        
        chapter_text = chapters_text[start:end]
        
        # Parse parts and articles
        parts, articles_outside = extract_parts_from_chapter(chapter_text)
        
        chapter = {
            "number": chapter_num,
            "title": clean_text(chapter_title)
        }
        
        if parts:
            chapter["parts"] = parts
        if articles_outside:
            chapter["articles"] = articles_outside
        
        chapters.append(chapter)
    
    return chapters


def parse_preamble(text: str) -> dict:
    """Parse the preamble section."""
    # Find preamble - from start until first CHAPTER
    preamble_match = re.search(r'PREAMBLE\s*(.*?)(?=CHAPTER\s+ONE)', text, re.DOTALL | re.IGNORECASE)
    
    if not preamble_match:
        return {"paragraphs": []}
    
    preamble_text = preamble_match.group(1)
    
    # Split into paragraphs, clean each
    lines = preamble_text.split('\n')
    paragraphs = []
    current_para = []
    
    for line in lines:
        # Skip page markers
        if re.search(r'Constitution of Kenya, 2010', line):
            continue
        
        stripped = line.strip()
        if stripped:
            current_para.append(stripped)
        elif current_para:
            paragraphs.append(' '.join(current_para))
            current_para = []
    
    if current_para:
        paragraphs.append(' '.join(current_para))
    
    # Filter out empty paragraphs
    paragraphs = [p for p in paragraphs if p.strip()]
    
    return {"paragraphs": paragraphs}


def parse_first_schedule(text: str) -> dict:
    """Parse First Schedule - Counties list."""
    counties = []
    
    # Pattern: number. County name
    county_pattern = re.compile(r'(\d+)\.\s*([A-Za-z\s\'/\-]+?)(?=\r?\n|\d+\.)')
    
    for match in county_pattern.finditer(text):
        num = int(match.group(1))
        name = clean_text(match.group(2))
        if name:
            counties.append({
                "number": num,
                "name": name
            })
    
    return {
        "number": 1,
        "title": "COUNTIES",
        "reference": "Article 6 (1)",
        "type": "list",
        "items": counties
    }


def parse_second_schedule(text: str) -> dict:
    """Parse Second Schedule - National Symbols."""
    sections = []
    
    # Find subsections (a), (b), (c), (d)
    section_pattern = re.compile(r'\(([a-d])\)\s*(THE\s+[A-Z\s]+)', re.IGNORECASE)
    
    for match in section_pattern.finditer(text):
        label = match.group(1)
        title = clean_text(match.group(2))
        sections.append({
            "label": label,
            "title": title
        })
    
    return {
        "number": 2,
        "title": "NATIONAL SYMBOLS",
        "reference": "Article 9 (2)",
        "type": "symbols",
        "sections": sections
    }


def parse_third_schedule(text: str) -> dict:
    """Parse Third Schedule - Oaths and Affirmations."""
    oaths = []
    
    # Pattern for oath headers
    oath_pattern = re.compile(r'(OATH\s+(?:OR\s+SOLEMN\s+AFFIRMATION\s+)?(?:OF\s+)?[A-Z\s/]+?)(?=\r?\n\s*I,)', re.IGNORECASE)
    
    for match in oath_pattern.finditer(text):
        title = clean_text(match.group(1))
        if title:
            oaths.append({
                "title": title
            })
    
    return {
        "number": 3,
        "title": "NATIONAL OATHS AND AFFIRMATIONS",
        "reference": "Articles 74, 141(3), 148(5) and 152(4)",
        "type": "oaths",
        "oaths": oaths
    }


def parse_fourth_schedule(text: str) -> dict:
    """Parse Fourth Schedule - Distribution of Functions."""
    parts = []
    
    # Part 1 - National Government
    part1_match = re.search(r'PART\s+1\s*[-–—]\s*NATIONAL\s+GOVERNMENT(.*?)(?=PART\s+2)', text, re.DOTALL | re.IGNORECASE)
    if part1_match:
        functions = []
        func_pattern = re.compile(r'(\d+)\.\s*([^0-9\n]+?)(?=\r?\n\s*\d+\.|\r?\nPART|\Z)')
        for match in func_pattern.finditer(part1_match.group(1)):
            num = int(match.group(1))
            func_text = clean_text(match.group(2))
            if func_text:
                functions.append({
                    "number": num,
                    "text": func_text
                })
        parts.append({
            "number": 1,
            "title": "NATIONAL GOVERNMENT",
            "functions": functions
        })
    
    # Part 2 - County Governments
    part2_match = re.search(r'PART\s+2\s*[-–—]\s*COUNTY\s+GOVERNMENTS(.*?)(?=FIFTH\s+SCHEDULE|\Z)', text, re.DOTALL | re.IGNORECASE)
    if part2_match:
        functions = []
        func_pattern = re.compile(r'(\d+)\.\s*([^0-9\n]+?)(?=\r?\n\s*\d+\.|\r?\n\d+\s+Constitution|\Z)')
        for match in func_pattern.finditer(part2_match.group(1)):
            num = int(match.group(1))
            func_text = clean_text(match.group(2))
            if func_text:
                functions.append({
                    "number": num,
                    "text": func_text
                })
        parts.append({
            "number": 2,
            "title": "COUNTY GOVERNMENTS",
            "functions": functions
        })
    
    return {
        "number": 4,
        "title": "DISTRIBUTION OF FUNCTIONS BETWEEN THE NATIONAL GOVERNMENT AND THE COUNTY GOVERNMENTS",
        "reference": "Articles 185(2), 186(1) and 187(2)",
        "type": "functions",
        "parts": parts
    }


def parse_fifth_schedule(text: str) -> dict:
    """Parse Fifth Schedule - Legislation to be enacted (table format)."""
    rows = []
    
    # Split into lines and process
    lines = text.split('\n')
    current_chapter = ""
    
    for i, line in enumerate(lines):
        line = line.strip()
        
        # Skip page markers and empty lines
        if not line or re.search(r'Constitution of Kenya, 2010', line):
            continue
        
        # Check for chapter header
        chapter_match = re.match(r'(CHAPTER\s+[A-Z]+\s*[-–—]\s*[A-Z\s]+)', line, re.IGNORECASE)
        if chapter_match:
            current_chapter = clean_text(chapter_match.group(1))
            continue
        
        # Check for article and time specification
        # Format: "Legislation description (Article X)" followed by time on next line or same line
        article_match = re.match(r'(.+?)\s*\(Article\s+(\d+(?:\s*\([^)]+\))?)\)', line, re.IGNORECASE)
        if article_match:
            description = clean_text(article_match.group(1))
            article_ref = article_match.group(2)
            
            # Look for time specification on next non-empty line
            time_spec = ""
            for j in range(i + 1, min(i + 3, len(lines))):
                next_line = lines[j].strip()
                if next_line and not re.search(r'Constitution of Kenya|CHAPTER|Article', next_line, re.IGNORECASE):
                    if re.match(r'(One|Two|Three|Four|Five|Six|18|[0-9]+)\s*(year|month)', next_line, re.IGNORECASE):
                        time_spec = clean_text(next_line)
                        break
            
            if current_chapter or description:
                rows.append({
                    "chapter": current_chapter,
                    "description": description,
                    "article": f"Article {article_ref}",
                    "timeSpecification": time_spec
                })
    
    return {
        "number": 5,
        "title": "LEGISLATION TO BE ENACTED BY PARLIAMENT",
        "reference": "Article 261 (1)",
        "type": "table",
        "rows": rows
    }


def parse_sixth_schedule(text: str) -> dict:
    """Parse Sixth Schedule - Transitional and Consequential Provisions."""
    parts = []
    
    # Pattern for parts
    part_pattern = re.compile(r'PART\s+(\d+)\s*[-–—]\s*([A-Z\s]+?)(?=\r?\n)', re.IGNORECASE)
    part_matches = list(part_pattern.finditer(text))
    
    for i, match in enumerate(part_matches):
        part_num = int(match.group(1))
        part_title = clean_text(match.group(2))
        
        start = match.end()
        end = part_matches[i + 1].start() if i + 1 < len(part_matches) else len(text)
        part_text = text[start:end]
        
        # Parse sections within part
        sections = []
        section_pattern = re.compile(r'(\d+)\.\s+(?:\(1\)\s*)?(.+?)(?=\r?\n\s*\d+\.|\Z)', re.DOTALL)
        
        for sec_match in section_pattern.finditer(part_text):
            sec_num = int(sec_match.group(1))
            sec_text = clean_text(sec_match.group(2))
            if sec_text:
                sections.append({
                    "number": sec_num,
                    "text": sec_text[:500] + "..." if len(sec_text) > 500 else sec_text
                })
        
        parts.append({
            "number": part_num,
            "title": part_title,
            "sections": sections
        })
    
    return {
        "number": 6,
        "title": "TRANSITIONAL AND CONSEQUENTIAL PROVISIONS",
        "reference": "Article 262",
        "type": "transitional",
        "parts": parts
    }


def parse_schedules(text: str) -> list:
    """Parse all six schedules."""
    schedules = []
    
    # Find where schedules start
    schedules_start = re.search(r'SCHEDULES\s+FIRST\s+SCHEDULE', text, re.IGNORECASE)
    if not schedules_start:
        return schedules
    
    schedules_text = text[schedules_start.start():]
    
    # Define schedule boundaries
    schedule_markers = [
        (r'FIRST\s+SCHEDULE', r'SECOND\s+SCHEDULE'),
        (r'SECOND\s+SCHEDULE', r'THIRD\s+SCHEDULE'),
        (r'THIRD\s+SCHEDULE', r'FOURTH\s+SCHEDULE'),
        (r'FOURTH\s+SCHEDULE', r'FIFTH\s+SCHEDULE'),
        (r'FIFTH\s+SCHEDULE', r'SIXTH\s+SCHEDULE'),
        (r'SIXTH\s+SCHEDULE', None)
    ]
    
    parsers = [
        parse_first_schedule,
        parse_second_schedule,
        parse_third_schedule,
        parse_fourth_schedule,
        parse_fifth_schedule,
        parse_sixth_schedule
    ]
    
    for i, (start_pattern, end_pattern) in enumerate(schedule_markers):
        start_match = re.search(start_pattern, schedules_text, re.IGNORECASE)
        if not start_match:
            continue
        
        start = start_match.start()
        if end_pattern:
            end_match = re.search(end_pattern, schedules_text, re.IGNORECASE)
            end = end_match.start() if end_match else len(schedules_text)
        else:
            end = len(schedules_text)
        
        schedule_text = schedules_text[start:end]
        schedule = parsers[i](schedule_text)
        schedules.append(schedule)
    
    return schedules


def parse_constitution(text: str) -> dict:
    """Main parser function that orchestrates all parsing."""
    result = {
        "metadata": {
            "title": "The Constitution of Kenya, 2010",
            "country": "Kenya",
            "year": 2010
        },
        "preamble": parse_preamble(text),
        "chapters": parse_chapters(text),
        "schedules": parse_schedules(text)
    }
    
    return result


def print_summary(data: dict):
    """Print parsing summary."""
    print("=" * 60)
    print("Parsing Summary")
    print("=" * 60)
    
    preamble_paras = len(data.get('preamble', {}).get('paragraphs', []))
    print(f"Preamble paragraphs: {preamble_paras}")
    
    chapters = data.get('chapters', [])
    print(f"Chapters: {len(chapters)}")
    
    total_articles = 0
    total_clauses = 0
    
    for chapter in chapters:
        chapter_articles = 0
        
        # Count articles in parts
        for part in chapter.get('parts', []):
            chapter_articles += len(part.get('articles', []))
            for article in part.get('articles', []):
                total_clauses += len(article.get('clauses', []))
        
        # Count articles directly in chapter
        chapter_articles += len(chapter.get('articles', []))
        for article in chapter.get('articles', []):
            total_clauses += len(article.get('clauses', []))
        
        total_articles += chapter_articles
        
        parts_count = len(chapter.get('parts', []))
        print(f"  Chapter {chapter['number']}: {chapter['title'][:40]}... - {chapter_articles} articles, {parts_count} parts")
    
    print(f"\nTotal articles: {total_articles}")
    print(f"Total clauses: {total_clauses}")
    
    schedules = data.get('schedules', [])
    print(f"\nSchedules: {len(schedules)}")
    for schedule in schedules:
        print(f"  Schedule {schedule['number']}: {schedule['title'][:40]}... ({schedule['type']})")


def main():
    # Paths
    script_dir = Path(__file__).parent
    project_root = script_dir.parent
    input_path = project_root / "composeApp" / "src" / "commonMain" / "composeResources" / "files" / "The_Constitution_of_Kenya_2010.txt"
    output_path = project_root / "composeApp" / "src" / "commonMain" / "composeResources" / "files" / "constitution.json"
    
    print("=" * 60)
    print("Constitution of Kenya Parser")
    print("=" * 60)
    print(f"Input:  {input_path}")
    print(f"Output: {output_path}")
    print()
    
    if not input_path.exists():
        print(f"ERROR: Input file not found at {input_path}")
        return 1
    
    # Read text
    print("Reading constitution text...")
    text = read_constitution_text(input_path)
    print(f"Text length: {len(text)} characters")
    print()
    
    # Parse
    print("Parsing constitution...")
    constitution = parse_constitution(text)
    
    # Print summary
    print_summary(constitution)
    print()
    
    # Save JSON
    print("Saving JSON...")
    output_path.parent.mkdir(parents=True, exist_ok=True)
    
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(constitution, f, indent=2, ensure_ascii=False)
    
    print(f"JSON saved to: {output_path}")
    print(f"JSON size: {output_path.stat().st_size:,} bytes")
    print()
    print("=" * 60)
    print("SUCCESS!")
    print("=" * 60)
    
    return 0


if __name__ == "__main__":
    exit(main())
