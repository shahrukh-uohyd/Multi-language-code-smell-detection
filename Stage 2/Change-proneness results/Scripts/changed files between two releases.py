#changed files(includes every java and c/c++ files) using pydriller and fault inducing commits
import os
import csv
from pydriller import Repository
from git import Repo, GitCommandError
from collections import defaultdict
from datetime import datetime

def detect_fault_inducing_commits(repo_path, release_pairs, output_dir="buggy_files_reports"):
    """
    Enhanced fault-inducing commit detection with better blame handling and debugging.
    """
    os.makedirs(output_dir, exist_ok=True)
    git_repo = Repo(repo_path)
    results = {}
    project_name = repo_path.split("/")[-1]
    if project_name=="vlc-android":
        project_name="vlc"

    for start_release, end_release in release_pairs:
        try:
            print(f"\nAnalyzing {start_release} to {end_release}")
            
            # Get commit range
            start_commit = git_repo.tags[start_release].commit
            end_commit = git_repo.tags[end_release].commit
            
            csv_filename = os.path.join(output_dir, f"{project_name}-{start_release}.csv")
            buggy_files = set()
            fix_count = 0
            
            with open(csv_filename, 'w', newline='') as csvfile:
                writer = csv.DictWriter(csvfile, fieldnames=[
                    'buggy_file_path', 'fix_commit_hash', 'fix_date',
                    'buggy_commit_hash', 'buggy_date', 'fix_message'
                ])
                writer.writeheader()
                
                # Traverse commits with PyDriller
                for commit in Repository(
                    repo_path,
                    since=start_commit.committed_datetime,
                    to=end_commit.committed_datetime,
                    only_modifications_with_file_types=['.java', '.cpp', '.c', '.h']  # Filter by file type
                ).traverse_commits():
                    
#                     if not is_fix_commit(commit.msg):
#                         continue
                    
                    fix_count += 1
                    print(f"  Fix commit: {commit.hash[:8]} - {commit.msg[:50]}...")
                    
                    for modified_file in commit.modified_files:
                        if not modified_file.new_path:
                            continue
                            
                        try:
                            # Get previous version of the file
                            old_path = modified_file.old_path or modified_file.new_path
                            previous_contents = git_repo.git.show(f"{commit.hash}^:{old_path}")
                            current_contents = git_repo.git.show(f"{commit.hash}:{modified_file.new_path}")
                            
                            # Get changed lines
                            diff = get_changed_lines(previous_contents, current_contents)
                            if not diff:
                                continue
                                
                            # Find blame for changed lines
                            blame_output = git_repo.git.blame(
                                '-w', '-l', '-p',  # -w ignores whitespace, -l shows long hashes
                                f"{commit.hash}^",  # Look at parent commit
                                '--', modified_file.new_path
                            )
                            
                            buggy_commits = parse_blame_for_lines(blame_output, diff)
                            
                            for buggy_hash in buggy_commits:
                                try:
                                    buggy_commit = git_repo.commit(buggy_hash)
                                    # Prefer old_path for fault-inducing context, fallback to new_path
                                    buggy_file_path = modified_file.old_path or modified_file.new_path

                                    buggy_files.add(buggy_file_path)
                                    writer.writerow({
                                        'buggy_file_path': buggy_file_path,
                                        'fix_commit_hash': commit.hash,
                                        'fix_date': commit.committer_date,
                                        'buggy_commit_hash': buggy_hash,
                                        'buggy_date': buggy_commit.committed_datetime,
                                        'fix_message': commit.msg[:200].replace('\n', ' ')
                                    })

                                    print(f"    Found buggy commit: {buggy_hash[:8]} for {modified_file.new_path}")
                                    
                                except Exception as e:
                                    print(f"    Error processing buggy commit: {str(e)}")
                                    continue
                                    
                        except GitCommandError as e:
                            print(f"    Error processing {modified_file.new_path}: {str(e)}")
                            continue
            
            print(f"\nSummary for {start_release} to {end_release}:")
            print(f"  Fix commits analyzed: {fix_count}")
            print(f"  Buggy files found: {len(buggy_files)}")
            results[(start_release, end_release)] = buggy_files
            
        except Exception as e:
            print(f"Error processing {start_release}-{end_release}: {str(e)}")
            continue
    
    return results

def get_changed_lines(old_content, new_content):
    """Identify changed lines between two file versions"""
    old_lines = old_content.splitlines()
    new_lines = new_content.splitlines()
    diff = []
    
    for i, (old_line, new_line) in enumerate(zip(old_lines, new_lines)):
        if old_line != new_line:
            diff.append(i+1)  # Line numbers start at 1
    
    # Handle added/removed lines at the end
    len_diff = len(new_lines) - len(old_lines)
    if len_diff > 0:
        diff.extend(range(len(old_lines)+1, len(new_lines)+1))
    
    return diff

def parse_blame_for_lines(blame_output, target_lines):
    """Parse blame output for specific line numbers."""
    commits = set()
    current_line = 0
    commit_hash = None  # Ensure this is always defined

    for line in blame_output.split('\n'):
        if len(line) >= 40 and re.match(r'^[0-9a-f]{40}', line):  # New commit hash line
            commit_hash = line.split()[0]
        elif line.startswith('filename '):
            current_line += 1
        elif line.startswith('\t'):
            current_line += 1
            if current_line in target_lines and commit_hash:
                commits.add(commit_hash)

    return commits


# Define the release pairs you want to analyze
release_pairs_to_analyze = [  ("V1_4_3", "REL1_5_STABLE-BASE"),
        ("REL1_5_STABLE-BASE", "V1_5_0b3"),
        ("V1_5_0b3", "V1_5_0"),
        ("V1_5_0", "V1_5_1b1"),
        ("V1_5_1b1", "V1_5_1b2"),
        ("V1_5_1b2", "V1_5_2"),
        ("V1_5_2", "V1_5_3"),
        ("V1_5_3", "V1_5_5")

]

# Run the analysis
results = detect_fault_inducing_commits(
    repo_path="cloned_abidi_latest/pljava", #Path to the project to be analyzed for changed files
    release_pairs=release_pairs_to_analyze,
    output_dir="buggy_smelly/abidi/general_changed"
)

# Access results programmatically if needed
for release_pair, buggy_files in results.items():
    print(f"Between {release_pair[0]} and {release_pair[1]}, found {len(buggy_files)} buggy files")