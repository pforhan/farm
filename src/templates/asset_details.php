
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo PROJECT_NAME; ?> - Asset Details</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <h1><?php echo PROJECT_NAME; ?> - Asset Details</h1>
    <h2><?php echo PROJECT_ACRONYM; ?></h2>

    <p>
        <a href="index.php?action=browse">Back to Browse</a> |
        <a href="index.php?action=upload">Upload New Asset</a> |
        <a href="index.php?action=search">Search Assets</a>
    </p>

    <?php if (!empty($asset_details)): ?>
        <h3>Asset: <?php echo htmlspecialchars($asset_details['asset_name']); ?> (ID: <?php echo htmlspecialchars($asset_details['asset_id']); ?>)</h3>
        <ul>
            <li><strong>Store:</strong> <?php echo htmlspecialchars($asset_details['store_name'] ?? 'N/A'); ?></li>
            <li><strong>Author:</strong> <?php echo htmlspecialchars($asset_details['author_name'] ?? 'N/A'); ?></li>
            <li><strong>License:</strong> <?php echo htmlspecialchars($asset_details['license_name'] ?? 'N/A'); ?></li>
            <li><strong>Link:</strong>
                <?php if (!empty($asset_details['link'])): ?>
                    <a href="<?php echo htmlspecialchars($asset_details['link']); ?>" target="_blank"><?php echo htmlspecialchars($asset_details['link']); ?></a>
                <?php else: ?>
                    N/A
                <?php endif; ?>
            </li>
            <li><strong>Tags:</strong> <?php echo empty($tag_details) ? 'None' : implode(', ', array_map('htmlspecialchars', $tag_details)); ?></li>
            <li><strong>Projects:</strong> <?php echo empty($project_details) ? 'None' : implode(', ', array_map('htmlspecialchars', $project_details)); ?></li>
        </ul>

        <h4>Files:</h4>
        <?php if (!empty($file_details)): ?>
            <ul>
                <?php foreach ($file_details as $file):
                    $public_file_path = '/uploads/' . htmlspecialchars($asset_details['asset_id']) . '/' . htmlspecialchars($file['file_name']);
                ?>
                    <li>
                        <strong>Name:</strong> <?php echo htmlspecialchars($file['file_name']); ?><br>
                        <strong>Type:</strong> <?php echo htmlspecialchars($file['file_type']); ?><br>
                        <strong>Size:</strong> <?php echo round($file['file_size'] / 1024 / 1024, 2); ?> MB<br>
                        <?php if (!empty($file['preview_path']) && strpos($file['file_type'], 'image/') === 0): ?>
                            <img src="<?php echo htmlspecialchars($file['preview_path']); ?>" alt="Preview" style="max-width: 200px; border-radius: 8px;">
                            <br>
                        <?php elseif (strpos($file['file_type'], 'audio/') === 0): ?>
                            <audio controls style="width: 100%; max-width: 300px;">
                                <source src="<?php echo $public_file_path; ?>" type="<?php echo htmlspecialchars($file['file_type']); ?>">
                                Your browser does not support the audio element.
                            </audio>
                            <br>
                        <?php elseif (strpos($file['file_type'], 'text/') === 0 || strpos($file['file_type'], 'application/json') === 0 || strpos($file['file_type'], 'application/xml') === 0): ?>
                            <!-- For text files, we'll just show the download link for now -->
                            <p>Text file. Click download to view.</p>
                        <?php endif; ?>
                        <a href="<?php echo $public_file_path; ?>" download>Download File</a>
                    </li>
                <?php endforeach; ?>
            </ul>
        <?php else: ?>
            <p>No files associated with this asset.</p>
        <?php endif; ?>

    <?php else: ?>
        <p>Asset not found.</p>
    <?php endif; ?>

    <script src="js/scripts.js"></script>
</body>
</html>
