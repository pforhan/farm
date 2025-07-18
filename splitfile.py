import os
import re
import sys

def extract_farm_project(input_file_path_raw):
    """
    Extracts the Farm Project files from a single text file.
    Useful for pulling updates from a gemini canvas.
    It expects file paths to be marked with '--- FILE START: farm/path/to/file.ext ---'
    The content of each file runs until the next '--- FILE START:' marker or EOF.
    """
    # Set the root directory for extracted files.
    # If set to ".", files will be extracted directly into the current working directory.
    # If set to "farm", a new "farm" directory will be created, and files will go inside.
    project_root = "."

    # Regex to capture the file path from the new delimiter line.
    # Made more robust to handle varying whitespace around "FILE START:" and the path itself.
    file_start_delimiter_pattern = re.compile(r"^---\s*FILE START:\s*(farm/.*)\s*---$")
    # Regex to detect Markdown code block delimiters (these are preserved in .md files)
    markdown_code_block_pattern = re.compile(r"^```[a-z]*$")

    # --- Pre-check and Setup ---
    if not input_file_path_raw:
        print("Usage: python extract_farm_project.py <input_file_path>")
        print("Please provide the path to the text file containing the project content.")
        sys.exit(1)

    # Resolve the absolute path of the input file
    input_file_path = os.path.realpath(input_file_path_raw)

    if not os.path.isfile(input_file_path):
        print(f"Error: Input file '{input_file_path}' not found or is not a regular file.")
        sys.exit(1)

    print(f"Starting extraction of Farm Project from '{input_file_path}'...")

    # Create the main project root directory if it doesn't exist
    os.makedirs(project_root, exist_ok=True)

    # Change to the project root directory so all subsequent paths are relative to it
    original_cwd = os.getcwd()
    os.chdir(project_root)
    print(f"Changed directory to: {os.getcwd()}") # Debugging: confirm current directory

    # Variables to track current file state
    current_file_path = None # Will be set to a string path
    is_markdown_file = False # Flag to know if the current file is a Markdown file
    file_content_lines = [] # Using a list to store lines, then join them

    def write_current_file():
        nonlocal current_file_path, is_markdown_file, file_content_lines
        print("DEBUG: Attempting to write file.") # Debugging
        if current_file_path and file_content_lines:
            full_path = os.path.join(os.getcwd(), current_file_path)
            os.makedirs(os.path.dirname(full_path), exist_ok=True) # Ensure directory exists
            try:
                # Join lines and remove the trailing newline if it exists (from the loop's appending)
                content_to_write = "".join(file_content_lines)
                if content_to_write.endswith('\n'):
                    content_to_write = content_to_write[:-1] # Remove trailing newline if it's there

                with open(full_path, 'w', encoding='utf-8') as f:
                    f.write(content_to_write)
                print(f"  Created: {current_file_path}")
            except IOError as e:
                print(f"Error writing file {current_file_path}: {e}")
        else:
            print(f"DEBUG: No file to write or content is empty. Path: '{current_file_path}', Content length: {len(''.join(file_content_lines))}") # Debugging

        # Reset state for the next file
        current_file_path = None
        is_markdown_file = False
        file_content_lines = []

    print("DEBUG: Entering file reading loop.") # Debugging
    try:
        with open(input_file_path, 'r', encoding='utf-8') as f:
            for line_num, line in enumerate(f, 1):
                # print(f"DEBUG: Read line {line_num}: '{line.strip()}'") # Verbose debugging

                # Detect new file start delimiter
                match_file_start = file_start_delimiter_pattern.match(line)
                if match_file_start:
                    print(f"DEBUG: Detected FILE START delimiter (line {line_num}): '{line.strip()}'") # Debugging
                    write_current_file() # Write content of previous file if any

                    # Strip any accidental leading/trailing whitespace from the captured path,
                    # then remove 'farm/' prefix and replace spaces.
                    current_file_path = match_file_start.group(1).strip().replace("farm/", "", 1).replace(" ", "_")

                    # Determine if it's a Markdown file based on its extension
                    is_markdown_file = current_file_path.lower().endswith(('.md', '.markdown'))

                    print(f"DEBUG: New file path set to: '{current_file_path}' (Is Markdown: {is_markdown_file})") # Debugging
                    continue # Skip to the next line, this line was just a delimiter

                # If we have a current_file_path set, append the line to its content
                if current_file_path is not None:
                    # If it's a non-Markdown file AND the line is a code block delimiter, skip it
                    if not is_markdown_file and markdown_code_block_pattern.match(line.strip()):
                        print(f"DEBUG: Skipping extraneous code block delimiter in non-Markdown file (line {line_num}): '{line.strip()}'")
                        continue # Do not append this line

                    file_content_lines.append(line) # Append the line as is, with its newline
                else:
                    # This case handles lines before the very first file delimiter or blank lines between files
                    if line.strip(): # Only log non-empty lines if they are not part of content
                        print(f"DEBUG: Ignoring line {line_num} (no current file selected yet): '{line.strip()}'")

    except FileNotFoundError:
        print(f"Error: Input file '{input_file_path}' not found during processing.")
        sys.exit(1)
    except Exception as e:
        print(f"An unexpected error occurred during file reading: {e}")
        sys.exit(1)

    print("DEBUG: Exited file reading loop.") # Debugging

    # Handle the very last file in the input file, if any content was collected
    write_current_file()

    # --- Post-extraction Steps ---
    os.chdir(original_cwd) # Change back to original directory for install.sh path
    print("Making install.sh executable...")
    install_script_path = os.path.join(project_root, "install.sh")
    if os.path.exists(install_script_path):
        os.chmod(install_script_path, 0o755) # Give execute permissions
        print(f"  '{install_script_path}' is now executable.")
    else:
        print(f"Warning: '{install_script_path}' not found. Ensure it was extracted correctly.")

    print(f"Extraction complete! The project files are now in the '{project_root}/' directory.")
    print(f"Navigate into '{project_root}/' and run './install.sh' to set up the database and dependencies.")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Error: No input file provided.")
        print("Usage: python extract_farm_project.py <input_file_path>")
        sys.exit(1)
    extract_farm_project(sys.argv[1])
