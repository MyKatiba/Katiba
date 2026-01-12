#!/usr/bin/env python3
"""
Convert constitution.yaml to constitution.json for easier loading in Kotlin.
"""
import json
import yaml
from pathlib import Path

def main():
    project_root = Path(__file__).parent.parent
    yaml_path = project_root / "composeApp" / "src" / "commonMain" / "composeResources" / "files" / "constitution.yaml"
    json_path = project_root / "composeApp" / "src" / "commonMain" / "composeResources" / "files" / "constitution.json"

    print(f"Reading YAML from: {yaml_path}")

    with open(yaml_path, 'r', encoding='utf-8') as f:
        data = yaml.safe_load(f)

    print(f"Loaded {len(data.get('chapters', []))} chapters")

    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

    print(f"JSON saved to: {json_path}")
    print(f"JSON size: {json_path.stat().st_size} bytes")

if __name__ == "__main__":
    main()

