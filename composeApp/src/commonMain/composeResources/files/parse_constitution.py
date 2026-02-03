#!/usr/bin/env python3
"""
Parse the Constitution of Kenya 2010 from text file and convert to JSON format.

This parser handles the complex nested structure:
- Parts (within chapters)
- Chapters (18 total)
- Articles (264 total)
- Clauses (numbered with (1), (2), etc.)
- SubClauses (lettered with a, b, c, d)
- MiniClauses (roman numerals i, ii, iii, iv)

It also handles special sections:
- Preamble (introductory text)
- Schedules (6 special sections with varying formats)
"""

import json
import re
import os
from typing import Dict, List, Any, Optional, Tuple


# ============================================================================
# Constants
# ============================================================================

CHAPTER_WORD_TO_NUM = {
    'ONE': 1, 'TWO': 2, 'THREE': 3, 'FOUR': 4, 'FIVE': 5,
    'SIX': 6, 'SEVEN': 7, 'EIGHT': 8, 'NINE': 9, 'TEN': 10,
    'ELEVEN': 11, 'TWELVE': 12, 'THIRTEEN': 13, 'FOURTEEN': 14,
    'FIFTEEN': 15, 'SIXTEEN': 16, 'SEVENTEEN': 17, 'EIGHTEEN': 18
}

ROMAN_NUMERALS = ['i', 'ii', 'iii', 'iv', 'v', 'vi', 'vii', 'viii', 'ix', 'x',
                  'xi', 'xii', 'xiii', 'xiv', 'xv', 'xvi', 'xvii', 'xviii', 'xix', 'xx']

# Article title to number mapping
ARTICLE_TITLES = {
    "sovereignty of the people": 1, "supremacy of this constitution": 2,
    "defence of this constitution": 3, "declaration of the republic": 4,
    "territory of kenya": 5, "devolution and access to services": 6,
    "national, official and other languages": 7, "state and religion": 8,
    "national symbols and national days": 9, "national values and principles of governance": 10,
    "culture": 11, "entitlements of citizens": 12, "retention and acquisition of citizenship": 13,
    "citizenship by birth": 14, "citizenship by registration": 15, "dual citizenship": 16,
    "revocation of citizenship": 17, "legislation on citizenship": 18,
    "rights and fundamental freedoms": 19, "application of bill of rights": 20,
    "implementation of rights and fundamental freedoms": 21, "enforcement of bill of rights": 22,
    "authority of courts to uphold and enforce the bill of rights": 23,
    "limitation of rights and fundamental freedoms": 24, "limitation of rights or fundamental freedoms": 24,
    "fundamental rights and freedoms that may not be limited": 25, "right to life": 26,
    "equality and freedom from discrimination": 27, "human dignity": 28,
    "freedom and security of the person": 29, "slavery, servitude and forced labour": 30,
    "privacy": 31, "freedom of conscience, religion, belief and opinion": 32,
    "freedom of expression": 33, "freedom of the media": 34, "access to information": 35,
    "freedom of association": 36, "assembly, demonstration, picketing and petition": 37,
    "political rights": 38, "freedom of movement and residence": 39,
    "protection of right to property": 40, "labour relations": 41, "environment": 42,
    "economic and social rights": 43, "language and culture": 44, "family": 45,
    "consumer rights": 46, "fair administrative action": 47, "access to justice": 48,
    "rights of arrested persons": 49, "fair hearing": 50,
    "rights of persons detained, held in custody or imprisoned": 51,
    "interpretation of this part": 52, "interpretation of part": 52, "children": 53,
    "persons with disabilities": 54, "youth": 55, "minorities and marginalised groups": 56,
    "older members of society": 57, "state of emergency": 58,
    "kenya national human rights and equality commission": 59, "principles of land policy": 60,
    "classification of land": 61, "public land": 62, "community land": 63, "private land": 64,
    "landholding by non-citizens": 65, "regulation of land use and property": 66,
    "national land commission": 67, "legislation on land": 68,
    "obligations in respect of the environment": 69, "enforcement of environmental rights": 70,
    "agreements relating to natural resources": 71, "legislation relating to the environment": 72,
    "responsibilities of leadership": 73, "oath of office of state officers": 74,
    "conduct of state officers": 75, "financial probity of state officers": 76,
    "restriction on activities of state officers": 77, "citizenship and leadership": 78,
    "legislation to establish the ethics and anti-corruption commission": 79,
    "legislation on leadership": 80, "general principles for the electoral system": 81,
    "legislation on elections": 82, "registration as a voter": 83,
    "candidates for election and political parties to comply with code of conduct": 84,
    "eligibility to stand as an independent candidate": 85, "voting": 86, "electoral disputes": 87,
    "independent electoral and boundaries commission": 88, "delimitation of electoral units": 89,
    "allocation of party list seats": 90, "basic requirements for political parties": 91,
    "legislation on political parties": 92, "establishment of parliament": 93,
    "role of parliament": 94, "role of the national assembly": 95, "role of the senate": 96,
    "membership of the national assembly": 97, "membership of the senate": 98,
    "qualifications and disqualifications for election as member of parliament": 99,
    "promotion of representation of marginalised groups": 100, "election of members of parliament": 101,
    "term of parliament": 102, "vacation of office of member of parliament": 103,
    "right of recall": 104, "determination of questions of membership": 105,
    "speakers and deputy speakers of parliament": 106, "presiding in parliament": 107,
    "party leaders": 108, "exercise of legislative powers": 109,
    "bills concerning county government": 110, "special bills concerning county governments": 111,
    "ordinary bills concerning county governments": 112, "mediation committees": 113,
    "money bills": 114, "presidential assent and referral": 115, "coming into force of laws": 116,
    "powers, privileges and immunities": 117, "public access and participation": 118,
    "right to petition parliament": 119, "official languages of parliament": 120, "quorum": 121,
    "voting in parliament": 122, "decisions of senate": 123, "committees and standing orders": 124,
    "power to call for evidence": 125, "location of sittings of parliament": 126,
    "parliamentary service commission": 127, "clerks and staff of parliament": 128,
    "principles of executive authority": 129, "the national executive": 130,
    "authority of the president": 131, "functions of the president": 132, "power of mercy": 133,
    "exercise of presidential powers during temporary incumbency": 134,
    "decisions of the president": 135, "election of the president": 136,
    "qualifications and disqualifications for election as president": 137,
    "procedure at presidential election": 138, "death before assuming office": 139,
    "questions as to validity of presidential election": 140, "assumption of office of president": 141,
    "term of office of president": 142, "term of office of the president": 142,
    "protection from legal proceedings": 143, "removal of president on grounds of incapacity": 144,
    "removal of president by impeachment": 145, "vacancy in the office of president": 146,
    "functions of the deputy president": 147, "election and swearing-in of deputy president": 148,
    "vacancy in the office of deputy president": 149, "removal of deputy president": 150,
    "remuneration and benefits of president and deputy president": 151, "cabinet": 152,
    "decisions, responsibility and accountability of the cabinet": 153, "secretary to the cabinet": 154,
    "principal secretaries": 155, "attorney-general": 156, "director of public prosecutions": 157,
    "removal and resignation of director of public prosecutions": 158, "judicial authority": 159,
    "independence of the judiciary": 160, "judicial offices and officers": 161,
    "system of courts": 162, "supreme court": 163, "court of appeal": 164, "high court": 165,
    "appointment of chief justice, deputy chief justice and other judges": 166,
    "tenure of office of the chief justice and other judges": 167, "removal from office": 168,
    "subordinate courts": 169, "kadhis' courts": 170, "kadhis courts": 170,
    "establishment of the judicial service commission": 171,
    "functions of the judicial service commission": 172, "judiciary fund": 173,
    "objects of devolution": 174, "principles of devolved government": 175,
    "county governments": 176, "membership of county assembly": 177,
    "speaker of a county assembly": 178, "county executive committees": 179,
    "election of county governor and deputy county governor": 180,
    "removal of a county governor": 181, "removal of a county government": 181,
    "vacancy in the office of county governor": 182, "functions of county executive committees": 183,
    "urban areas and cities": 184, "legislative authority of county assemblies": 185,
    "respective functions and powers of national and county governments": 186,
    "transfer of functions and powers between levels of government": 187,
    "boundaries of counties": 188, "cooperation between national and county governments": 189,
    "support for county governments": 190, "conflict of laws": 191,
    "suspension of a county government": 192, "suspension of county government": 192,
    "qualifications for election as member of county assembly": 193,
    "vacation of office of member of county assembly": 194,
    "county assembly power to summon witnesses": 195,
    "public participation and county assembly powers, privileges and immunities": 196,
    "county assembly gender balance and diversity": 197, "county government during transition": 198,
    "publication of county legislation": 199, "legislation on chapter": 200,
    "principles of public finance": 201, "equitable sharing of national revenue": 202,
    "equitable share and other financial laws": 203, "equalisation fund": 204,
    "consultation on financial legislation affecting counties": 205,
    "consolidated fund and other public funds": 206, "revenue funds for county governments": 207,
    "contingencies fund": 208, "power to impose taxes and charges": 209, "imposition of tax": 210,
    "borrowing by national government": 211, "borrowing by counties": 212,
    "loan guarantees by national government": 213, "public debt": 214,
    "commission on revenue allocation": 215, "functions of the commission on revenue allocation": 216,
    "division of revenue": 217, "annual division and allocation of revenue bills": 218,
    "transfer of equitable share": 219, "form, content and timing of budgets": 220,
    "budget estimates and annual appropriation bill": 221, "expenditure before annual budget is passed": 222,
    "supplementary appropriation": 223, "county appropriation bills": 224, "financial control": 225,
    "accounts and audit of public entities": 226, "procurement of public goods and services": 227,
    "controller of budget": 228, "auditor-general": 229, "salaries and remuneration commission": 230,
    "central bank of kenya": 231, "values and principles of public service": 232,
    "the public service commission": 233, "functions and powers of the public service commission": 234,
    "staffing of county governments": 235, "protection of public officers": 236,
    "teachers service commission": 237, "principles of national security": 238,
    "national security organs": 239, "establishment of the national security council": 240,
    "establishment of kenya defence forces and defence council": 241,
    "establishment of defence forces and defence council": 241,
    "establishment of national intelligence service": 242,
    "establishment of the national police service": 243,
    "objects and functions of the national police service": 244,
    "command of the national police service": 245, "national police service commission": 246,
    "other police services": 247, "application of chapter": 248,
    "objects, authority and funding of commissions and independent offices": 249,
    "composition, appointment and terms of office": 250, "removal from office": 251,
    "general functions and powers": 252, "incorporation of commissions and independent offices": 253,
    "reporting by commissions and independent offices": 254, "amendment of this constitution": 255,
    "amendment by parliamentary initiative": 256, "amendment by popular initiative": 257,
    "enforcement of this constitution": 258, "construing this constitution": 259,
    "interpretation": 260, "consequential legislation": 261,
    "transitional and consequential provisions": 262, "effective date": 263,
    "repeal of previous constitution": 264,
}


# ============================================================================
# Helper Functions
# ============================================================================

def clean_line(line: str) -> str:
    """Clean a line by stripping and removing page markers."""
    line = line.strip()
    if re.search(r'Constitution of Kenya,?\s*2010\s*\d*', line, re.IGNORECASE):
        return ""
    return line


def normalize_title(title: str) -> str:
    """Normalize title for matching."""
    t = title.lower().strip()
    t = re.sub(r'[—–-]', '-', t)
    t = t.replace("'", "'").replace("'", "'")
    t = re.sub(r'\s+', ' ', t)
    return t


def get_article_number(title: str, last_num: int) -> int:
    """Get article number from title."""
    normalized = normalize_title(title)
    if normalized in ARTICLE_TITLES:
        return ARTICLE_TITLES[normalized]
    # Try partial match
    for known, num in ARTICLE_TITLES.items():
        if normalized.startswith(known) or known.startswith(normalized):
            return num
    return last_num + 1


# ============================================================================
# Parsing Functions
# ============================================================================

def parse_mini_clauses(text: str) -> Tuple[str, List[Dict]]:
    """Parse mini-clauses (roman numerals) from text."""
    mini_clauses = []

    # Split by roman numeral patterns
    parts = re.split(r'\(([ivxlc]+)\)', text, flags=re.IGNORECASE)

    if len(parts) > 1:
        main_text = parts[0].strip()
        for i in range(1, len(parts), 2):
            if i + 1 < len(parts):
                label = parts[i].lower()
                if label in ROMAN_NUMERALS:
                    mini_clauses.append({
                        "label": label,
                        "text": parts[i + 1].strip()
                    })
        return main_text, mini_clauses

    return text, []


def parse_subclauses(text: str) -> List[Dict]:
    """Parse subclauses from text."""
    subclauses = []

    # Split by subclause patterns (a), (b) or just a, b at line start
    lines = text.split('\n')
    current_label = None
    current_text = []

    for line in lines:
        line = clean_line(line)
        if not line:
            continue

        # Check for (a), (b) pattern
        match = re.match(r'^\(([a-z])\)\s*(.*)$', line)
        if match:
            if current_label:
                full_text = ' '.join(current_text).strip()
                main_text, minis = parse_mini_clauses(full_text)
                subclauses.append({
                    "label": current_label,
                    "text": main_text,
                    "miniClauses": minis
                })
            current_label = match.group(1)
            current_text = [match.group(2)] if match.group(2) else []
            continue

        # Check for standalone letter pattern (a word, b word)
        match = re.match(r'^([a-z])\s+(.+)$', line)
        if match:
            potential = match.group(1)
            expected = chr(ord(current_label) + 1) if current_label else 'a'
            if potential == expected:
                if current_label:
                    full_text = ' '.join(current_text).strip()
                    main_text, minis = parse_mini_clauses(full_text)
                    subclauses.append({
                        "label": current_label,
                        "text": main_text,
                        "miniClauses": minis
                    })
                current_label = potential
                current_text = [match.group(2)]
                continue

        # Continue current subclause
        if current_label:
            current_text.append(line)

    # Don't forget last one
    if current_label:
        full_text = ' '.join(current_text).strip()
        main_text, minis = parse_mini_clauses(full_text)
        subclauses.append({
            "label": current_label,
            "text": main_text,
            "miniClauses": minis
        })

    return subclauses


def parse_clauses(lines: List[str]) -> List[Dict]:
    """Parse clauses from article lines."""
    clauses = []
    current_num = ""
    current_text = []

    for line in lines:
        line = clean_line(line)
        if not line:
            continue

        # Check for "ArticleNum. (ClauseNum)" format - e.g., "27. (1) text..."
        match = re.match(r'^\d+\.\s*\((\d+)\)\s*(.*)$', line)
        if match:
            # Save previous clause
            if current_num or current_text:
                text = '\n'.join(current_text)
                subclauses = parse_subclauses(text)
                if subclauses:
                    main_lines = []
                    for l in current_text:
                        if not re.match(r'^[\(]?[a-z][\)]?\s', l):
                            main_lines.append(l)
                    text = ' '.join(main_lines).strip()
                clauses.append({
                    "number": current_num,
                    "text": text,
                    "subClauses": subclauses
                })
            current_num = match.group(1)
            current_text = [match.group(2)] if match.group(2) else []
            continue

        # Check for numbered clause (1), (2)
        match = re.match(r'^\((\d+)\)\s*(.*)$', line)
        if match:
            # Save previous clause
            if current_num or current_text:
                text = '\n'.join(current_text)
                subclauses = parse_subclauses(text)
                if subclauses:
                    # Remove subclause text from main text
                    main_lines = []
                    for l in current_text:
                        if not re.match(r'^[\(]?[a-z][\)]?\s', l):
                            main_lines.append(l)
                    text = ' '.join(main_lines).strip()
                clauses.append({
                    "number": current_num,
                    "text": text,
                    "subClauses": subclauses
                })
            current_num = match.group(1)
            current_text = [match.group(2)] if match.group(2) else []
            continue

        current_text.append(line)

    # Save last clause
    if current_num or current_text:
        text = '\n'.join(current_text)
        subclauses = parse_subclauses(text)
        if subclauses:
            main_lines = []
            for l in current_text:
                if not re.match(r'^[\(]?[a-z][\)]?\s', l):
                    main_lines.append(l)
            text = ' '.join(main_lines).strip()
        clauses.append({
            "number": current_num,
            "text": text,
            "subClauses": subclauses
        })

    # Handle articles with no numbered clauses
    if not clauses and lines:
        text = ' '.join(clean_line(l) for l in lines if clean_line(l))
        clauses.append({
            "number": "",
            "text": text,
            "subClauses": []
        })

    return clauses


def parse_articles(content: str, chapter_num: int) -> List[Dict]:
    """Parse articles from chapter content."""
    articles = []
    lines = content.split('\n')

    # Find article boundaries by looking for capitalized titles ending with period
    article_starts = []
    for i, line in enumerate(lines):
        line = clean_line(line)
        if not line:
            continue
        # Skip part headers
        if line.upper().startswith('PART '):
            continue
        # Check for article title (capitalized, ends with period)
        match = re.match(r'^([A-Z][^.]+(?:\s+[a-z][^.]*)*)\.$', line)
        if match:
            title = match.group(1)
            if len(title) < 100:
                article_starts.append((i, title))

    # Parse each article
    last_num = 0
    for idx, (start, title) in enumerate(article_starts):
        end = article_starts[idx + 1][0] if idx + 1 < len(article_starts) else len(lines)
        article_lines = lines[start + 1:end]

        num = get_article_number(title, last_num)
        last_num = num

        clauses = parse_clauses(article_lines)

        articles.append({
            "number": num,
            "title": title,
            "clauses": clauses
        })

    return articles


def parse_preamble(content: str) -> str:
    """Extract preamble text."""
    # Find preamble after table of contents
    match = re.search(r'We,\s+the\s+people\s+of\s+Kenya', content, re.IGNORECASE)
    if not match:
        return ""

    start = match.start()
    # Find end (CHAPTER ONE)
    end_match = re.search(r'CHAPTER\s+ONE', content[start:], re.IGNORECASE)
    if end_match:
        end = start + end_match.start()
    else:
        end = start + 2000

    preamble_text = content[start:end]
    # Clean up
    lines = [clean_line(l) for l in preamble_text.split('\n')]
    lines = [l for l in lines if l]
    return ' '.join(lines)


def parse_chapters(content: str) -> List[Dict]:
    """Parse all chapters."""
    chapters = []

    # Find preamble location to skip table of contents
    preamble_match = re.search(r'We,\s+the\s+people\s+of\s+Kenya', content, re.IGNORECASE)
    if preamble_match:
        start_pos = preamble_match.start()
    else:
        start_pos = 0

    # Find chapter boundaries
    chapter_pattern = r'CHAPTER\s+(ONE|TWO|THREE|FOUR|FIVE|SIX|SEVEN|EIGHT|NINE|TEN|ELEVEN|TWELVE|THIRTEEN|FOURTEEN|FIFTEEN|SIXTEEN|SEVENTEEN|EIGHTEEN)[—\-–]([^\n]+)'

    matches = list(re.finditer(chapter_pattern, content[start_pos:], re.IGNORECASE))

    # Find where schedules start
    schedules_match = re.search(r'SCHEDULES?\s+FIRST\s+SCHEDULE', content[start_pos:], re.IGNORECASE)
    schedules_pos = schedules_match.start() if schedules_match else len(content) - start_pos

    seen = set()
    for idx, match in enumerate(matches):
        if match.start() > schedules_pos:
            break

        chapter_word = match.group(1).upper()
        chapter_num = CHAPTER_WORD_TO_NUM.get(chapter_word)
        if not chapter_num or chapter_num in seen:
            continue
        seen.add(chapter_num)

        chapter_title = match.group(2).strip()

        # Get chapter content
        ch_start = match.end()
        if idx + 1 < len(matches) and matches[idx + 1].start() < schedules_pos:
            ch_end = matches[idx + 1].start()
        else:
            ch_end = schedules_pos

        chapter_content = content[start_pos + ch_start:start_pos + ch_end]

        # Find parts
        parts = []
        for part_match in re.finditer(r'PART\s+(\d+)[—\-–]([^\n]+)', chapter_content, re.IGNORECASE):
            parts.append({
                "number": int(part_match.group(1)),
                "title": part_match.group(2).strip()
            })

        # Parse articles
        articles = parse_articles(chapter_content, chapter_num)

        chapters.append({
            "number": chapter_num,
            "title": chapter_title,
            "parts": parts,
            "articles": articles
        })

    # Sort by chapter number
    chapters.sort(key=lambda x: x["number"])

    return chapters


# ============================================================================
# Schedule Parsing
# ============================================================================

def parse_schedule_1(content: str) -> Dict:
    """Parse First Schedule: Counties."""
    counties = [
        "Mombasa", "Kwale", "Kilifi", "Tana River", "Lamu", "Taita/Taveta",
        "Garissa", "Wajir", "Mandera", "Marsabit", "Isiolo", "Meru",
        "Tharaka-Nithi", "Embu", "Kitui", "Machakos", "Makueni", "Nyandarua",
        "Nyeri", "Kirinyaga", "Murang'a", "Kiambu", "Turkana", "West Pokot",
        "Samburu", "Trans Nzoia", "Uasin Gishu", "Elgeyo/Marakwet", "Nandi",
        "Baringo", "Laikipia", "Nakuru", "Narok", "Kajiado", "Kericho",
        "Bomet", "Kakamega", "Vihiga", "Bungoma", "Busia", "Siaya", "Kisumu",
        "Homa Bay", "Migori", "Kisii", "Nyamira", "Nairobi City"
    ]
    return {"counties": [{"number": i + 1, "name": name} for i, name in enumerate(counties)]}


def parse_schedule_2(content: str) -> Dict:
    """Parse Second Schedule: National Symbols."""
    return {
        "nationalFlag": {
            "description": "Three major strips of equal width coloured from top to bottom black, red and green and separated by narrow white strips, with a symmetrical shield and white spears superimposed centrally."
        },
        "nationalAnthem": {
            "verses": [
                {
                    "number": 1,
                    "kiswahili": "Ee Mungu nguvu yetu, Ilete baraka kwetu. Haki iwe ngao na mlinzi, Natukae na undugu. Amani na uhuru, Raha tupate na ustawi.",
                    "english": "O God of all creation, Bless this our land and nation. Justice be our shield and defender, May we dwell in unity. Peace and liberty, Plenty be found within our borders."
                },
                {
                    "number": 2,
                    "kiswahili": "Amkeni ndugu zetu, Tufanye sote bidii. Nasi tujitoe kwa nguvu, Nchi yetu ya Kenya. Tunayoipenda, Tuwe tayari kuilinda.",
                    "english": "Let one and all arise, With hearts both strong and true. Service be our earnest endeavour, And our Homeland of Kenya. Heritage of splendour, Firm may we stand to defend."
                },
                {
                    "number": 3,
                    "kiswahili": "Natujenge taifa letu, Ee, ndio wajibu wetu. Kenya istahili heshima, Tuungane mikono. Pamoja kazini, Kila siku tuwe na shukrani.",
                    "english": "Let all with one accord, In common bond united. Build this our nation together, And the glory of Kenya. The fruit of our labour, Fill every heart with thanksgiving."
                }
            ]
        },
        "coatOfArms": {"description": "The Coat of Arms with two lions, shield, and crossed spears on a mount with motto 'Harambee'."},
        "publicSeal": {"description": "The Public Seal of Kenya as prescribed by law."}
    }


def parse_schedule_3(content: str) -> Dict:
    """Parse Third Schedule: National Oaths."""
    oaths = []
    lines = content.split('\n')
    current_oath = None
    current_text = []

    for line in lines:
        line = clean_line(line)
        if not line:
            continue

        if 'OATH' in line.upper() or 'AFFIRMATION' in line.upper():
            if current_oath and current_text:
                oaths.append({"title": current_oath, "text": ' '.join(current_text)})
            current_oath = line
            current_text = []
        elif current_oath:
            current_text.append(line)

    if current_oath and current_text:
        oaths.append({"title": current_oath, "text": ' '.join(current_text)})

    return {"oaths": oaths}


def parse_schedule_4(content: str) -> Dict:
    """Parse Fourth Schedule: Distribution of Functions."""
    result = {"nationalGovernment": [], "countyGovernments": []}

    lines = content.split('\n')
    current_part = None
    current_func = None
    current_subs = []

    for line in lines:
        line = clean_line(line)
        if not line:
            continue

        if 'PART 1' in line.upper() or 'NATIONAL GOVERNMENT' in line.upper():
            if current_func and current_part:
                result[current_part].append({"number": current_func[0], "function": current_func[1], "subFunctions": current_subs})
            current_part = "nationalGovernment"
            current_func = None
            current_subs = []
            continue

        if 'PART 2' in line.upper() or 'COUNTY GOVERNMENT' in line.upper():
            if current_func and current_part:
                result[current_part].append({"number": current_func[0], "function": current_func[1], "subFunctions": current_subs})
            current_part = "countyGovernments"
            current_func = None
            current_subs = []
            continue

        if not current_part:
            continue

        # Numbered function
        match = re.match(r'^(\d+)\.\s*(.+)$', line)
        if match:
            if current_func:
                result[current_part].append({"number": current_func[0], "function": current_func[1], "subFunctions": current_subs})
            current_func = (int(match.group(1)), match.group(2))
            current_subs = []
            continue

        # Sub-function
        match = re.match(r'^\(([a-z])\)\s*(.+)$', line)
        if match and current_func:
            current_subs.append({"label": match.group(1), "text": match.group(2)})

    if current_func and current_part:
        result[current_part].append({"number": current_func[0], "function": current_func[1], "subFunctions": current_subs})

    return result


def parse_schedule_5(content: str) -> Dict:
    """Parse Fifth Schedule: Legislation to be Enacted (table format)."""
    legislation = []

    # Parse as a table: Description (Article X) followed by Time on next line
    lines = content.split('\n')
    current_chapter = None
    pending_item = None

    for line in lines:
        line = clean_line(line)
        if not line:
            continue

        # Chapter header (CHAPTER TWO-REPUBLIC, etc.)
        if line.upper().startswith('CHAPTER '):
            current_chapter = line
            continue

        # Skip column headers and page markers
        if 'Chapter and Article' in line or 'Time Specification' in line:
            continue

        # Try to match legislation entry: Description (Article X)
        # Skip the schedule header itself
        if 'FIFTH SCHEDULE' in line.upper():
            continue
        match = re.match(r'^(.+?)\s*\(Article\s*(\d+(?:\s*\([^)]+\))?)\)\s*$', line, re.IGNORECASE)
        if match:
            # Save previous pending item if exists
            if pending_item:
                legislation.append(pending_item)

            pending_item = {
                "description": match.group(1).strip(),
                "article": match.group(2).strip(),
                "timeSpecification": "",
                "chapter": current_chapter
            }
            continue

        # Check if this is a time specification (contains "year" or "months")
        if pending_item and ('year' in line.lower() or 'month' in line.lower()):
            pending_item["timeSpecification"] = line
            legislation.append(pending_item)
            pending_item = None
            continue

    # Don't forget last pending item
    if pending_item:
        legislation.append(pending_item)

    return {"legislation": legislation}


def parse_schedule_6(content: str) -> Dict:
    """Parse Sixth Schedule: Transitional Provisions."""
    sections = []
    lines = content.split('\n')

    current_section = None
    current_content = []
    current_part = None

    for line in lines:
        line = clean_line(line)
        if not line:
            continue

        # Part header
        if line.upper().startswith('PART '):
            if current_section:
                sections.append({
                    "number": current_section[0],
                    "title": current_section[1],
                    "content": ' '.join(current_content),
                    "part": current_part
                })
            match = re.match(r'PART\s+(\d+)[—\-–](.+)', line, re.IGNORECASE)
            if match:
                current_part = f"Part {match.group(1)}: {match.group(2).strip()}"
            current_section = None
            current_content = []
            continue

        # Section header
        match = re.match(r'^([A-Z][^.]+(?:\s+[a-z][^.]*)*)\.$', line)
        if match and len(match.group(1)) < 60:
            if current_section:
                sections.append({
                    "number": current_section[0],
                    "title": current_section[1],
                    "content": ' '.join(current_content),
                    "part": current_part
                })
            current_section = (len(sections) + 1, match.group(1))
            current_content = []
            continue

        if current_section:
            current_content.append(line)

    if current_section:
        sections.append({
            "number": current_section[0],
            "title": current_section[1],
            "content": ' '.join(current_content),
            "part": current_part
        })

    return {"sections": sections}


def parse_schedules(content: str) -> List[Dict]:
    """Parse all schedules."""
    schedules = []

    schedule_info = [
        ("FIRST SCHEDULE", 1, "COUNTIES", "Article 6(1)", parse_schedule_1),
        ("SECOND SCHEDULE", 2, "NATIONAL SYMBOLS", "Article 9(2)", parse_schedule_2),
        ("THIRD SCHEDULE", 3, "NATIONAL OATHS AND AFFIRMATIONS", "Articles 74, 141(3), 148(5), 152(4)", parse_schedule_3),
        ("FOURTH SCHEDULE", 4, "DISTRIBUTION OF FUNCTIONS", "Articles 185(2), 186(1), 187(2)", parse_schedule_4),
        ("FIFTH SCHEDULE", 5, "LEGISLATION TO BE ENACTED BY PARLIAMENT", "Article 261(1)", parse_schedule_5),
        ("SIXTH SCHEDULE", 6, "TRANSITIONAL AND CONSEQUENTIAL PROVISIONS", "Article 262", parse_schedule_6),
    ]

    # First, find where the main SCHEDULES section starts (after last article, before FIRST SCHEDULE)
    schedules_section_match = re.search(r'SCHEDULES\s+FIRST\s+SCHEDULE', content, re.IGNORECASE)
    if schedules_section_match:
        schedules_start = schedules_section_match.start()
    else:
        # Fallback: find FIRST SCHEDULE after the main content
        schedules_start = len(content) // 2  # Assume schedules are in second half

    # Find schedule positions - only search after schedules_start
    search_content = content[schedules_start:]
    positions = []
    for pattern, num, title, ref, parser in schedule_info:
        # Look for schedule header patterns - may have tab or whitespace before (Article
        header_pattern = rf'{pattern}\s*[\t\n\r]+\s*\(Article'
        match = re.search(header_pattern, search_content, re.IGNORECASE)
        if not match:
            # Some schedules have (Article on same line after tab
            header_pattern = rf'{pattern}\s+\(Article'
            match = re.search(header_pattern, search_content, re.IGNORECASE)
        if match:
            positions.append((schedules_start + match.start(), num, title, ref, parser))

    positions.sort(key=lambda x: x[0])

    # Extract content for each schedule
    for i, (start, num, title, ref, parser) in enumerate(positions):
        if i + 1 < len(positions):
            end = positions[i + 1][0]
        else:
            # End at SUBSIDIARY LEGISLATION or end of content
            sub_match = re.search(r'SUBSIDIARY LEGISLATION', content[start:], re.IGNORECASE)
            end = start + sub_match.start() if sub_match else len(content)

        schedule_content = content[start:end]
        parsed = parser(schedule_content)

        schedules.append({
            "number": num,
            "title": title,
            "reference": ref,
            "content": parsed
        })

    return schedules


# ============================================================================
# Main Functions
# ============================================================================

def parse_constitution(file_path: str) -> Dict[str, Any]:
    """Parse the constitution from a text file."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    result = {
        "preamble": parse_preamble(content),
        "chapters": parse_chapters(content),
        "schedules": parse_schedules(content)
    }

    return result


def validate_result(result: Dict) -> List[str]:
    """Validate parsing results."""
    issues = []

    if not result.get("preamble"):
        issues.append("Preamble is missing")

    chapters = result.get("chapters", [])
    if len(chapters) != 18:
        issues.append(f"Expected 18 chapters, found {len(chapters)}")

    total_articles = sum(len(ch.get("articles", [])) for ch in chapters)
    if total_articles < 200:
        issues.append(f"Found only {total_articles} articles (expected ~264)")

    schedules = result.get("schedules", [])
    if len(schedules) != 6:
        issues.append(f"Expected 6 schedules, found {len(schedules)}")

    return issues


def main():
    """Main entry point."""
    import argparse

    parser = argparse.ArgumentParser(description="Parse Constitution of Kenya 2010")
    parser.add_argument('input_file', nargs='?', help="Input text file")
    parser.add_argument('-o', '--output', help="Output JSON file")
    parser.add_argument('-v', '--verbose', action='store_true', help="Verbose output")

    args = parser.parse_args()

    # Find input file
    script_dir = os.path.dirname(os.path.abspath(__file__))

    if args.input_file:
        input_file = args.input_file
    else:
        candidates = [
            os.path.join(script_dir, "The_Constitution_of_Kenya_2010.txt"),
            os.path.join(script_dir, "CONSTITUTION-OF-KENYA-2010.txt"),
        ]
        input_file = next((f for f in candidates if os.path.exists(f)), None)
        if not input_file:
            print("Error: No input file found")
            return 1

    output_file = args.output or os.path.join(script_dir, "constitution_of_kenya.json")

    print(f"Parsing: {input_file}")

    result = parse_constitution(input_file)

    # Summary
    chapters = result.get("chapters", [])
    total_articles = sum(len(ch.get("articles", [])) for ch in chapters)

    print(f"\nResults:")
    print(f"  Preamble: {len(result.get('preamble', ''))} chars")
    print(f"  Chapters: {len(chapters)}")
    print(f"  Articles: {total_articles}")
    print(f"  Schedules: {len(result.get('schedules', []))}")

    if args.verbose:
        for ch in chapters:
            print(f"    Chapter {ch['number']}: {len(ch['articles'])} articles")

    issues = validate_result(result)
    if issues:
        print("\nWarnings:")
        for issue in issues:
            print(f"  - {issue}")

    # Write output
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(result, f, indent=2, ensure_ascii=False)

    print(f"\nOutput: {output_file}")
    return 0


if __name__ == "__main__":
    exit(main())
