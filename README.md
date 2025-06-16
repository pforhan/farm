
# Farm: Digital Asset Manager

## Files, Assets, Resources, Metadata

Farm is a simple, open-source web application for tracking and managing your digital game assets. It allows you to organize assets by store, author, license, tags, and projects, and provides a way to upload files (including zip archives), preview graphics, and play audio.

## Features

* **Asset Tracking:** Store details like asset name, source store, link, author, and license.
* **Flexible Organization:** Categorize assets with zero or more tags and associate them with zero or more projects.
* **File Management:** Upload individual files or zip archives.
* **Automatic Extraction:** Zip files are automatically extracted, and their contents are individually tracked.
* **Enhanced Tagging:**
    * Automatically tags assets based on the uploaded zip filename and source URL.
    * Extracts additional tags from directory names within uploaded zip archives (e.g., `20x20/static` will add "20x20" and "static" as tags).
* **Media Previews:** View previews of image files (JPG, PNG, GIF) and play audio files (WAV, MP3, OGG).
* **Text File Handling:** Supports uploading and managing various text-based files (TXT, MD, HTML, JSON, XML).
* **Search & Browse:** Search by name, tag, type, and graphics size. Browse all assets with quick access to details.

## Project Structure

