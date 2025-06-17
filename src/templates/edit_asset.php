<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo PROJECT_NAME; ?> - Edit Asset</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <h1><?php echo PROJECT_NAME; ?> - Edit Asset</h1>
    <h2><?php echo PROJECT_ACRONYM; ?></h2>

    <?php include __DIR__ . '/partials/navigation.php'; ?>

    <?php if (isset($message) && !empty($message)) { ?>
        <p style="color: green; font-weight: bold;"><?php echo htmlspecialchars($message); ?></p>
    <?php } ?>

    <?php if (!empty($asset_details)): ?>
    <form action="index.php?action=edit_asset" method="post">
        <input type="hidden" name="asset_id" value="<?php echo htmlspecialchars($asset_details['asset_id']); ?>">

        <label for="asset_name">Asset Name:</label>
        <input type="text" name="asset_name" id="asset_name" value="<?php echo htmlspecialchars($asset_details['asset_name'] ?? ''); ?>" required><br><br>

        <label for="link">Source URL (Link):</label>
        <input type="url" name="link" id="link" value="<?php echo htmlspecialchars($asset_details['link'] ?? ''); ?>" placeholder="http://example.com/source"><br><br>

        <label for="store_name">Store:</label>
        <input type="text" name="store_name" id="store_name" value="<?php echo htmlspecialchars($asset_details['store_name'] ?? ''); ?>" placeholder="e.g., Unity Asset Store"><br><br>

        <label for="author_name">Author:</label>
        <input type="text" name="author_name" id="author_name" value="<?php echo htmlspecialchars($asset_details['author_name'] ?? ''); ?>" placeholder="e.g., Jane Doe"><br><br>

        <label for="license_name">License:</label>
        <input type="text" name="license_name" id="license_name" value="<?php echo htmlspecialchars($asset_details['license_name'] ?? ''); ?>" placeholder="e.g., MIT, Royalty-Free"><br><br>

        <label for="tags">Tags (comma-separated):</label>
        <input type="text" name="tags" id="tags" value="<?php echo htmlspecialchars(implode(', ', $tag_details)); ?>" placeholder="e.g., 2D, character, pixelart"><br><br>

        <label for="projects">Projects (comma-separated):</label>
        <input type="text" name="projects" id="projects" value="<?php echo htmlspecialchars(implode(', ', $project_details)); ?>" placeholder="e.g., MyGameTitle, RPG"><br><br>

        <input type="submit" value="Update Asset" name="submit_update">
    </form>
    <?php else: ?>
        <p>Asset not found for editing.</p>
    <?php endif; ?>

    <script src="js/scripts.js"></script>
</body>
</html>