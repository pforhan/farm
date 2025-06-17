// public/js/scripts.js
// This file can be used for any client-side interactivity.
// For now, it's a placeholder.

document.addEventListener('DOMContentLoaded', function() {
    console.log('Farm Digital Asset Manager - Client-side scripts loaded.');

    // Automatically set asset name from file name
    const fileInput = document.getElementById('file');
    const assetNameInput = document.getElementById('asset_name');

    if (fileInput && assetNameInput) {
        fileInput.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                const fileName = this.files[0].name;
                const nameWithoutExtension = fileName.split('.').slice(0, -1).join('.');
                assetNameInput.value = nameWithoutExtension;
            }
        });
    }

    // Example: Add a simple form submission message
    const uploadForm = document.querySelector('form');
    if (uploadForm) {
        uploadForm.addEventListener('submit', function() {
            // You could add a loading spinner or disable the submit button here
            console.log('Form submitted!');
        });
    }
});
