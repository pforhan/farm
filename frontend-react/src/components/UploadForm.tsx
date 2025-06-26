import React, { useState } from 'react';
import { FarmApiClient } from '../api/farmApiClient';
import { Loader2, Upload as UploadIcon, XCircle } from 'lucide-react'; // Import icons

interface UploadFormProps {
  onUploadSuccess: (assetId: number, msg: string) => void;
  onUploadError: (errorMsg: string) => void;
}

export const UploadForm: React.FC<UploadFormProps> = ({ onUploadSuccess, onUploadError }) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [assetName, setAssetName] = useState<string>('');
  const [link, setLink] = useState<string>('');
  const [storeName, setStoreName] = useState<string>('');
  const [authorName, setAuthorName] = useState<string>('');
  const [licenseName, setLicenseName] = useState<string>('');
  const [tags, setTags] = useState<string>('');
  const [projects, setProjects] = useState<string>('');

  const [isUploading, setIsUploading] = useState<boolean>(false);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files ? event.target.files[0] : null;
    setSelectedFile(file);
    if (file) {
      // Auto-fill asset name from file name (without extension)
      setAssetName(file.name.split('.').slice(0, -1).join('.'));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!selectedFile) {
      onUploadError('Please select a file to upload.');
      return;
    }
    if (assetName.trim() === '') {
      onUploadError('Asset name cannot be empty.');
      return;
    }

    setIsUploading(true);
    try {
      const responseMessage = await FarmApiClient.uploadAsset(
        assetName.trim(),
        link.trim() === '' ? null : link.trim(),
        storeName.trim() === '' ? null : storeName.trim(),
        authorName.trim() === '' ? null : authorName.trim(),
        licenseName.trim() === '' ? null : licenseName.trim(),
        tags.trim() === '' ? null : tags.trim(),
        projects.trim() === '' ? null : projects.trim(),
        selectedFile,
      );

      // Extract asset ID from the success message
      const assetIdMatch = responseMessage.match(/Asset ID: (\d+)/);
      const uploadedAssetId = assetIdMatch ? parseInt(assetIdMatch[1], 10) : null;

      if (uploadedAssetId !== null) {
        onUploadSuccess(uploadedAssetId, responseMessage);
        // Clear form fields after successful upload
        setSelectedFile(null);
        setAssetName('');
        setLink('');
        setStoreName('');
        setAuthorName('');
        setLicenseName('');
        setTags('');
        setProjects('');
      } else {
        onUploadError(`Upload succeeded, but could not parse Asset ID from response: ${responseMessage}`);
      }
    } catch (e: any) {
      onUploadError(`File upload process failed: ${e.message}`);
    } finally {
      setIsUploading(false);
    }
  };

  const handleClearForm = () => {
    setSelectedFile(null);
    setAssetName('');
    setLink('');
    setStoreName('');
    setAuthorName('');
    setLicenseName('');
    setTags('');
    setProjects('');
    setIsUploading(false);
  };

  return (
    <div className="space-y-6">
      <h2 className="text-3xl font-bold text-gray-800 mb-4">Upload New Asset</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="file-input" className="block text-sm font-medium text-gray-700 mb-1">
            Select File to Upload:
          </label>
          <input
            type="file"
            id="file-input"
            onChange={handleFileChange}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
          />
          {selectedFile && (
            <p className="mt-2 text-sm text-gray-600">Selected: {selectedFile.name}</p>
          )}
        </div>

        <div>
          <label htmlFor="assetName" className="block text-sm font-medium text-gray-700 mb-1">
            Asset Name
          </label>
          <input
            type="text"
            id="assetName"
            value={assetName}
            onChange={(e) => setAssetName(e.target.value)}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
          />
        </div>

        <div>
          <label htmlFor="link" className="block text-sm font-medium text-gray-700 mb-1">
            Source URL (Link)
          </label>
          <input
            type="url"
            id="link"
            value={link}
            onChange={(e) => setLink(e.target.value)}
            placeholder="http://example.com/source"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
          />
        </div>

        <div>
          <label htmlFor="storeName" className="block text-sm font-medium text-gray-700 mb-1">
            Store
          </label>
          <input
            type="text"
            id="storeName"
            value={storeName}
            onChange={(e) => setStoreName(e.target.value)}
            placeholder="e.g., Unity Asset Store"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
          />
        </div>

        <div>
          <label htmlFor="authorName" className="block text-sm font-medium text-gray-700 mb-1">
            Author
          </label>
          <input
            type="text"
            id="authorName"
            value={authorName}
            onChange={(e) => setAuthorName(e.target.value)}
            placeholder="e.g., Jane Doe"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
          />
        </div>

        <div>
          <label htmlFor="licenseName" className="block text-sm font-medium text-gray-700 mb-1">
            License
          </label>
          <input
            type="text"
            id="licenseName"
            value={licenseName}
            onChange={(e) => setLicenseName(e.target.value)}
            placeholder="e.g., MIT, Royalty-Free"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
          />
        </div>

        <div>
          <label htmlFor="tags" className="block text-sm font-medium text-gray-700 mb-1">
            Initial Tags (comma-separated)
          </label>
          <input
            type="text"
            id="tags"
            value={tags}
            onChange={(e) => setTags(e.target.value)}
            placeholder="e.g., 2D, character, pixelart"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
          />
        </div>

        <div>
          <label htmlFor="projects" className="block text-sm font-medium text-gray-700 mb-1">
            Projects (comma-separated)
          </label>
          <input
            type="text"
            id="projects"
            value={projects}
            onChange={(e) => setProjects(e.target.value)}
            placeholder="e.g., MyGameTitle, RPG"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
          />
        </div>

        <div className="flex justify-between mt-6">
          <button
            type="button"
            onClick={handleClearForm}
            className="px-5 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-opacity-50 flex items-center space-x-2 transition duration-200"
            disabled={isUploading}
          >
            <XCircle size={18} />
            <span>Clear Form</span>
          </button>
          <button
            type="submit"
            className="px-5 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-50 flex items-center space-x-2 transition duration-200"
            disabled={isUploading || !selectedFile || assetName.trim() === ''}
          >
            {isUploading ? <Loader2 className="animate-spin" size={18} /> : <UploadIcon size={18} />}
            <span>{isUploading ? 'Uploading...' : 'Upload Asset'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};