import React, { useState, useEffect } from 'react';

// Define the Asset interface based on your Kotlin data class
interface Asset {
  assetId: number;
  assetName: string;
  link: string | null;
  storeName: string | null;
  authorName: string | null;
  licenseName: string | null;
  tags: string[]; // This is defined as string[]
  projects: string[]; // This is defined as string[]
  files: FileDetail[];
  previewThumbnail: string | null;
}

// Define the FileDetail interface
interface FileDetail {
  fileId: number;
  assetId: number;
  fileName: string;
  filePath: string;
  publicPath: string;
  fileSize: number;
  fileType: string;
  previewPath: string | null;
}

function App() {
  const [activeView, setActiveView] = useState('browse'); // 'browse', 'upload', 'details', 'edit', 'search'
  const [assets, setAssets] = useState<Asset[]>([]);
  const [selectedAsset, setSelectedAsset] = useState<Asset | null>(null);
  const [message, setMessage] = useState<string>('');
  const [isError, setIsError] = useState<boolean>(false); // New state to indicate error
  const [searchQuery, setSearchQuery] = useState<string>('');

  // State for the upload form
  const [uploadAssetName, setUploadAssetName] = useState('');
  const [uploadLink, setUploadLink] = useState('');
  const [uploadStoreName, setUploadStoreName] = useState('');
  const [uploadAuthorName, setUploadAuthorName] = useState('');
  const [uploadLicenseName, setUploadLicenseName] = useState('');
  const [uploadTags, setUploadTags] = useState('');
  const [uploadProjects, setUploadProjects] = useState('');
  const [uploadFile, setUploadFile] = useState<File | null>(null);

  // State for the edit form
  const [editAssetName, setEditAssetName] = useState('');
  const [editLink, setEditLink] = useState('');
  const [editStoreName, setEditStoreName] = useState('');
  const [editAuthorName, setEditAuthorName] = useState('');
  const [editLicenseName, setEditLicenseName] = useState('');
  const [editTags, setEditTags] = useState('');
  const [editProjects, setEditProjects] = useState('');

  const API_BASE_URL = '/api'; // Ktor serves API from /api

  useEffect(() => {
    if (activeView === 'browse') {
      fetchAssets();
    }
  }, [activeView]);

  const fetchAssets = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/assets`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data: Asset[] = await response.json();
      console.log("Fetched assets:", data); // Added logging
      setAssets(data);
      setMessage('');
      setIsError(false);
    } catch (error) {
      console.error("Error fetching assets:", error);
      setMessage(`Error fetching assets: ${error instanceof Error ? error.message : String(error)}`);
      setIsError(true);
    }
  };

  const fetchAssetDetails = async (id: number) => {
    try {
      const response = await fetch(`${API_BASE_URL}/assets/${id}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data: Asset = await response.json();
      console.log("Fetched asset details:", data); // Added logging
      setSelectedAsset(data);
      setMessage('');
      setIsError(false);
      setActiveView('details');
    } catch (error) {
      console.error("Error fetching asset details:", error);
      setMessage(`Error fetching asset details: ${error instanceof Error ? error.message : String(error)}`);
      setIsError(true);
    }
  };

  const handleFileUpload = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!uploadFile) {
      setMessage('Please select a file to upload.');
      setIsError(true);
      return;
    }

    const formData = new FormData();
    formData.append('file', uploadFile);
    formData.append('asset_name', uploadAssetName);
    formData.append('link', uploadLink);
    formData.append('store_name', uploadStoreName);
    formData.append('author_name', uploadAuthorName);
    formData.append('license_name', uploadLicenseName);
    formData.append('tags', uploadTags);
    formData.append('projects', uploadProjects);

    try {
      const response = await fetch(`${API_BASE_URL}/assets/upload`, {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        setMessage('File uploaded successfully!');
        setIsError(false);
        // Clear form fields
        setUploadAssetName('');
        setUploadLink('');
        setUploadStoreName('');
        setUploadAuthorName('');
        setUploadLicenseName('');
        setUploadTags('');
        setUploadProjects('');
        setUploadFile(null);
        // Optionally, navigate to browse or details page
        setActiveView('browse');
        fetchAssets(); // Refresh asset list
      } else {
        const errorText = await response.text();
        setMessage(`Upload failed: ${errorText}`);
        setIsError(true);
      }
    } catch (error) {
      console.error("Upload error:", error);
      setMessage(`Upload failed: ${error instanceof Error ? error.message : String(error)}`);
      setIsError(true);
    }
  };

  const handleEditAsset = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!selectedAsset) return;

    const updateData = {
      assetName: editAssetName,
      link: editLink,
      storeName: editStoreName,
      authorName: editAuthorName,
      licenseName: editLicenseName,
      tagsString: editTags,
      projectsString: editProjects,
    };

    try {
      const response = await fetch(`${API_BASE_URL}/assets/${selectedAsset.assetId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updateData),
      });

      if (response.ok) {
        setMessage('Asset updated successfully!');
        setIsError(false);
        setActiveView('browse');
        fetchAssets(); // Refresh asset list
      } else {
        const errorText = await response.text();
        setMessage(`Update failed: ${errorText}`);
        setIsError(true);
      }
    } catch (error) {
      console.error("Update error:", error);
      setMessage(`Update failed: ${error instanceof Error ? error.message : String(error)}`);
      setIsError(true);
    }
  };

  const handleDeleteAsset = async (assetId: number) => {
    // Using a simple confirm for now, would replace with a custom modal in a full app
    if (!window.confirm(`Are you sure you want to delete asset ID ${assetId}? This action cannot be undone.`)) {
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/assets/${assetId}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        setMessage(`Asset ID ${assetId} deleted successfully.`);
        setIsError(false);
        fetchAssets(); // Refresh asset list
      } else {
        const errorText = await response.text();
        setMessage(`Delete failed: ${errorText}`);
        setIsError(true);
      }
    } catch (error) {
      console.error("Delete error:", error);
      setMessage(`Delete failed: ${error instanceof Error ? error.message : String(error)}`);
      setIsError(true);
    }
  };

  const handleSearch = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!searchQuery.trim()) {
      setMessage('Please enter a search query.');
      setIsError(true);
      return;
    }
    try {
      const response = await fetch(`${API_BASE_URL}/assets/search?query=${encodeURIComponent(searchQuery)}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data: Asset[] = await response.json();
      console.log("Search results:", data); // Added logging
      setAssets(data); // Update assets with search results
      setMessage('');
      setIsError(false);
      setActiveView('search'); // Stay on search view
    } catch (error) {
      console.error("Error searching assets:", error);
      setMessage(`Error searching assets: ${error instanceof Error ? error.message : String(error)}`);
      setIsError(true);
    }
  };


  const renderView = () => {
    console.log("Rendering view:", activeView, "Selected Asset:", selectedAsset); // Added logging

    switch (activeView) {
      case 'upload':
        return (
          <div className="p-4">
            <h2 className="text-2xl font-bold mb-4">Upload New Asset</h2>
            {/* Message display for upload form */}
            {/* Moved to main return block for consistent display */}
            <form onSubmit={handleFileUpload} className="bg-white p-6 rounded-lg shadow-md">
              <div className="mb-4">
                <label htmlFor="asset_name" className="block text-gray-700 text-sm font-bold mb-2">Asset Name:</label>
                <input
                  type="text"
                  id="asset_name"
                  value={uploadAssetName}
                  onChange={(e) => setUploadAssetName(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div className="mb-4">
                <label htmlFor="link" className="block text-gray-700 text-sm font-bold mb-2">Source URL (Link):</label>
                <input
                  type="url"
                  id="link"
                  value={uploadLink}
                  onChange={(e) => setUploadLink(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="http://example.com/source"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="store_name" className="block text-gray-700 text-sm font-bold mb-2">Store:</label>
                <input
                  type="text"
                  id="store_name"
                  value={uploadStoreName}
                  onChange={(e) => setUploadStoreName(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="e.g., Unity Asset Store"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="author_name" className="block text-gray-700 text-sm font-bold mb-2">Author:</label>
                <input
                  type="text"
                  id="author_name"
                  value={uploadAuthorName}
                  onChange={(e) => setUploadAuthorName(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="e.g., Jane Doe"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="license_name" className="block text-gray-700 text-sm font-bold mb-2">License:</label>
                <input
                  type="text"
                  id="license_name"
                  value={uploadLicenseName}
                  onChange={(e) => setUploadLicenseName(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="e.g., MIT, Royalty-Free"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="tags" className="block text-gray-700 text-sm font-bold mb-2">Initial Tags (comma-separated):</label>
                <input
                  type="text"
                  id="tags"
                  value={uploadTags}
                  onChange={(e) => setUploadTags(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="e.g., 2D, character, pixelart"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="projects" className="block text-gray-700 text-sm font-bold mb-2">Projects (comma-separated):</label>
                <input
                  type="text"
                  id="projects"
                  value={uploadProjects}
                  onChange={(e) => setUploadProjects(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="e.g., MyGameTitle, RPG"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="file" className="block w-full text-gray-700 text-sm font-bold mb-2">Select file to upload:</label>
                <input
                  type="file"
                  id="file"
                  onChange={(e) => {
                    if (e.target.files) {
                      setUploadFile(e.target.files[0]);
                      // Automatically set asset name from file name if not already set
                      if (!uploadAssetName) {
                        const fileName = e.target.files[0].name;
                        setUploadAssetName(fileName.split('.').slice(0, -1).join('.'));
                      }
                    }
                  }}
                  className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                  required
                />
              </div>
              <button
                type="submit"
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
              >
                Upload Asset
              </button>
            </form>
          </div>
        );
      case 'details':
        if (!selectedAsset) {
          console.error("selectedAsset is null in details view."); // Added logging
          return <p className="p-4 text-red-500">Asset not found or failed to load details.</p>;
        }
        return (
          <div className="p-4">
            <h2 className="text-2xl font-bold mb-4">Asset Details</h2>
            <div className="bg-white p-6 rounded-lg shadow-md">
              <h3 className="text-xl font-semibold mb-2">{selectedAsset.assetName} (ID: {selectedAsset.assetId})</h3>
              <ul className="list-disc list-inside mb-4">
                <li><strong>Store:</strong> {selectedAsset.storeName || 'N/A'}</li>
                <li><strong>Author:</strong> {selectedAsset.authorName || 'N/A'}</li>
                <li><strong>License:</strong> {selectedAsset.licenseName || 'N/A'}</li>
                <li><strong>Link:</strong> {selectedAsset.link ? <a href={selectedAsset.link} target="_blank" rel="noopener noreferrer" className="text-blue-500 hover:underline">{selectedAsset.link}</a> : 'N/A'}</li>
                {/* Updated to safely access tags and projects */}
                <li><strong>Tags:</strong> {(selectedAsset.tags ?? []).join(', ') || 'None'}</li>
                <li><strong>Projects:</strong> {(selectedAsset.projects ?? []).join(', ') || 'None'}</li>
              </ul>

              <h4 className="text-lg font-semibold mb-2">Files:</h4>
              {selectedAsset.files.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {selectedAsset.files.map(file => (
                    <div key={file.fileId} className="border p-4 rounded-lg shadow-sm bg-gray-50">
                      <p><strong>Name:</strong> {file.fileName}</p>
                      <p><strong>Type:</strong> {file.fileType}</p>
                      <p><strong>Size:</strong> {(file.fileSize / (1024 * 1024)).toFixed(2)} MB</p>
                      {file.previewPath && file.fileType.startsWith('image/') && (
                        <img src={file.previewPath} alt="Preview" className="max-w-full h-auto rounded-md mt-2" />
                      )}
                      {file.fileType.startsWith('audio/') && (
                        <audio controls src={file.publicPath} className="w-full mt-2"></audio>
                      )}
                      <a href={file.publicPath} download className="text-blue-500 hover:underline mt-2 inline-block">Download File</a>
                    </div>
                  ))}
                </div>
              ) : (
                <p>No files associated with this asset.</p>
              )}
            </div>
          </div>
        );
      case 'edit':
        if (!selectedAsset) {
          console.error("selectedAsset is null in edit view."); // Added logging
          return <p className="p-4 text-red-500">Asset not selected for editing or failed to load details.</p>;
        }
        return (
          <div className="p-4">
            <h2 className="text-2xl font-bold mb-4">Edit Asset</h2>
            {/* Message display for edit form */}
            {/* Moved to main return block for consistent display */}
            <form onSubmit={handleEditAsset} className="bg-white p-6 rounded-lg shadow-md">
              <div className="mb-4">
                <label htmlFor="edit_asset_name" className="block text-gray-700 text-sm font-bold mb-2">Asset Name:</label>
                <input
                  type="text"
                  id="edit_asset_name"
                  value={editAssetName}
                  onChange={(e) => setEditAssetName(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div className="mb-4">
                <label htmlFor="edit_link" className="block text-gray-700 text-sm font-bold mb-2">Source URL (Link):</label>
                <input
                  type="url"
                  id="edit_link"
                  value={editLink}
                  onChange={(e) => setEditLink(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="http://example.com/source"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="edit_store_name" className="block text-gray-700 text-sm font-bold mb-2">Store:</label>
                <input
                  type="text"
                  id="edit_store_name"
                  value={editStoreName}
                  onChange={(e) => setEditStoreName(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="e.g., Unity Asset Store"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="edit_author_name" className="block text-gray-700 text-sm font-bold mb-2">Author:</label>
                <input
                  type="text"
                  id="edit_author_name"
                  value={editAuthorName}
                  onChange={(e) => setEditAuthorName(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="e.g., Jane Doe"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="edit_license_name" className="block text-gray-700 text-sm font-bold mb-2">License:</label>
                <input
                  type="text"
                  id="edit_license_name"
                  value={editLicenseName}
                  onChange={(e) => setEditLicenseName(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="e.g., MIT, Royalty-Free"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="edit_tags" className="block text-gray-700 text-sm font-bold mb-2">Tags (comma-separated):</label>
                <input
                  type="text"
                  id="edit_tags"
                  value={editTags}
                  onChange={(e) => setEditTags(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="e.g., 2D, character, pixelart"
                />
              </div>
              <div className="mb-4">
                <label htmlFor="edit_projects" className="block text-gray-700 text-sm font-bold mb-2">Projects (comma-separated):</label>
                <input
                  type="text"
                  id="edit_projects"
                  value={editProjects}
                  onChange={(e) => setEditProjects(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="e.g., MyGameTitle, RPG"
                />
              </div>
              <button
                type="submit"
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
              >
                Update Asset
              </button>
            </form>
          </div>
        );
      case 'search':
        return (
          <div className="p-4">
            <h2 className="text-2xl font-bold mb-4">Search Assets</h2>
            {/* Message display for search form */}
            {/* Moved to main return block for consistent display */}
            <form onSubmit={handleSearch} className="bg-white p-6 rounded-lg shadow-md mb-4">
              <div className="mb-4">
                <label htmlFor="search_query" className="block text-gray-700 text-sm font-bold mb-2">Search Query:</label>
                <input
                  type="text"
                  id="search_query"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="Enter name, tag, type, or size (e.g., 512x512)"
                />
              </div>
              <button
                type="submit"
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
              >
                Search
              </button>
            </form>

            <h3 className="text-xl font-semibold mb-4">Search Results:</h3>
            {assets.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="min-w-full bg-white rounded-lg shadow-md">
                  <thead>
                    <tr>
                      <th className="py-3 px-4 border-b text-left">ID</th>
                      <th className="py-3 px-4 border-b text-left">Preview</th>
                      <th className="py-3 px-4 border-b text-left">Name</th>
                      <th className="py-3 px-4 border-b text-left">Link</th>
                      <th className="py-3 px-4 border-b text-left">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {assets.map(asset => (
                      <tr key={asset.assetId} className="hover:bg-gray-50">
                        <td className="py-3 px-4 border-b">{asset.assetId}</td>
                        <td className="py-3 px-4 border-b">
                          {asset.previewThumbnail ? (
                            <img src={asset.previewThumbnail} alt="Preview" className="w-12 h-12 object-cover rounded-md" />
                          ) : (
                            <span className="text-gray-500">N/A</span>
                          )}
                        </td>
                        <td className="py-3 px-4 border-b">{asset.assetName}</td>
                        <td className="py-3 px-4 border-b">
                          {asset.link ? (
                            <a href={asset.link} target="_blank" rel="noopener noreferrer" className="text-blue-500 hover:underline">View Link</a>
                          ) : (
                            <span className="text-gray-500">N/A</span>
                          )}
                        </td>
                        <td className="py-3 px-4 border-b">
                          <button
                            onClick={() => fetchAssetDetails(asset.assetId)}
                            className="text-blue-500 hover:underline mr-2"
                          >
                            Details
                          </button>
                          <button
                            onClick={() => {
                              setSelectedAsset(asset);
                              setEditAssetName(asset.assetName);
                              setEditLink(asset.link || '');
                              setEditStoreName(asset.storeName || '');
                              setEditAuthorName(asset.authorName || '');
                              setEditLicenseName(asset.licenseName || '');
                              // Safely initialize editTags and editProjects
                              setEditTags((asset.tags ?? []).join(', '));
                              setEditProjects((asset.projects ?? []).join(', '));
                              setActiveView('edit');
                            }}
                            className="text-yellow-600 hover:underline mr-2"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDeleteAsset(asset.assetId)}
                            className="text-red-600 hover:underline"
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="p-4">No assets found matching your search query.</p>
            )}
          </div>
        );
      case 'browse':
      default:
        return (
          <div className="p-4">
            <h2 className="text-2xl font-bold mb-4">Browse Assets</h2>
            {/* Message display for browse view */}
            {/* Moved to main return block for consistent display */}
            {assets.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="min-w-full bg-white rounded-lg shadow-md">
                  <thead>
                    <tr>
                      <th className="py-3 px-4 border-b text-left">ID</th>
                      <th className="py-3 px-4 border-b text-left">Preview</th>
                      <th className="py-3 px-4 border-b text-left">Name</th>
                      <th className="py-3 px-4 border-b text-left">Link</th>
                      <th className="py-3 px-4 border-b text-left">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {assets.map(asset => (
                      <tr key={asset.assetId} className="hover:bg-gray-50">
                        <td className="py-3 px-4 border-b">{asset.assetId}</td>
                        <td className="py-3 px-4 border-b">
                          {asset.previewThumbnail ? (
                            <img src={asset.previewThumbnail} alt="Preview" className="w-12 h-12 object-cover rounded-md" />
                          ) : (
                            <span className="text-gray-500">N/A</span>
                          )}
                        </td>
                        <td className="py-3 px-4 border-b">{asset.assetName}</td>
                        <td className="py-3 px-4 border-b">
                          {asset.link ? (
                            <a href={asset.link} target="_blank" rel="noopener noreferrer" className="text-blue-500 hover:underline">View Link</a>
                          ) : (
                            <span className="text-gray-500">N/A</span>
                          )}
                        </td>
                        <td className="py-3 px-4 border-b">
                          <button
                            onClick={() => fetchAssetDetails(asset.assetId)}
                            className="text-blue-500 hover:underline mr-2"
                          >
                            Details
                          </button>
                          <button
                            onClick={() => {
                              setSelectedAsset(asset);
                              setEditAssetName(asset.assetName);
                              setEditLink(asset.link || '');
                              setEditStoreName(asset.storeName || '');
                              setEditAuthorName(asset.authorName || '');
                              setEditLicenseName(asset.licenseName || '');
                              // Safely initialize editTags and editProjects
                              setEditTags((asset.tags ?? []).join(', '));
                              setEditProjects((asset.projects ?? []).join(', '));
                              setActiveView('edit');
                            }}
                            className="text-yellow-600 hover:underline mr-2"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDeleteAsset(asset.assetId)}
                            className="text-red-600 hover:underline"
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="p-4">No assets found. Start by uploading one!</p>
            )}
          </div>
        );
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 font-sans text-gray-900">
      <header className="bg-blue-600 text-white p-4 shadow-md">
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-3xl font-extrabold">Farm Digital Asset Manager</h1>
          <nav>
            <button onClick={() => setActiveView('upload')} className="bg-blue-700 hover:bg-blue-800 text-white font-bold py-2 px-4 rounded-full mr-2 transition duration-300 ease-in-out transform hover:scale-105">
              Upload New Asset
            </button>
            <button onClick={() => setActiveView('browse')} className="bg-blue-700 hover:bg-blue-800 text-white font-bold py-2 px-4 rounded-full mr-2 transition duration-300 ease-in-out transform hover:scale-105">
              Browse Assets
            </button>
            <button onClick={() => setActiveView('search')} className="bg-blue-700 hover:bg-blue-800 text-white font-bold py-2 px-4 rounded-full transition duration-300 ease-in-out transform hover:scale-105">
              Search Assets
            </button>
          </nav>
        </div>
      </header>
      <main className="container mx-auto mt-8 p-4">
        {/* Centralized message display */}
        {message && (
          <div className={`p-3 mb-4 rounded-md ${isError ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
            {message}
          </div>
        )}
        {renderView()}
      </main>
    </div>
  );
}

export default App;
