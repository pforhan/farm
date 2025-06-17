<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo PROJECT_NAME; ?> - Upload</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <h1><?php echo PROJECT_NAME; ?> - Upload New Asset</h1>
    <h2><?php echo PROJECT_ACRONYM; ?></h2>

    <?php if (isset($message) && !empty($message)) { ?>
        <p style="color: green; font-weight: bold;"><?php echo htmlspecialchars($message); ?></p>
    <?php } ?>

    <form action="index.php?action=upload" method="post" enctype="multipart/form-data">
        <label for="file">Select file to upload:</label>
        <input type="file" name="file" id="file" required><br><br>

        <label for="asset_name">Asset Name:</label>
        <input type="text" name="asset_name" id="asset_name" required><br><br>

        <label for="link">Source URL (Link):</label>
        <input type="url" name="link" id="link" placeholder="http://example.com/source"><br><br>

        <label for="store_name">Store:</label>
        <input type="text" name="store_name" id="store_name" placeholder="e.g., Unity Asset Store"><br><br>

        <label for="author_name">Author:</label>
        <input type="text" name="author_name" id="author_name" placeholder="e.g., Jane Doe"><br><br>

        <label for="license_name">License:</label>
        <input type="text" name="license_name" id="license_name" placeholder="e.g., MIT, Royalty-Free"><br><br>

        <label for="tags">Initial Tags (comma-separated):</label>
        <input type="text" name="tags" id="tags" placeholder="e.g., 2D, character, pixelart"><br><br>

        <label for="projects">Projects (comma-separated):</label>
        <input type="text" name="projects" id="projects" placeholder="e.g., MyGameTitle, RPG"><br><br>

        <input type="submit" value="Upload Asset" name="submit">
    </form>
    <p>
        <a href="index.php?action=browse">Browse Assets</a> |
        <a href="index.php?action=search">Search Assets</a>
    </p>
    <script src="js/scripts.js"></script>
</body>
</html>