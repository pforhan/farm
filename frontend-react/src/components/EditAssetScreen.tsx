import { useState, useEffect } from 'react'; // Removed unused React and Asset imports
import { UpdateAssetRequest } from '../types/models'; // Removed unused Asset import
import { FarmApiClient } from '../api/farmApiClient';
import { Loader2, Save, XCircle } from 'lucide-react'; // Icons

interface EditAssetScreenProps {
  assetId: number;
  onUpdateSuccess: (assetId: number, msg: string) => void;
  onCancel: () => void;
  onMessage: (msg: string, isError?: boolean) => void;
}

export const EditAssetScreen: React.FC<EditAssetScreenProps> = ({
  assetId,
  onUpdateSuccess,
  onCancel,
  onMessage,
}) => {
  const [assetName, setAssetName] = useState<string>('');
  const [link, setLink] = useState<string>('');
  const [storeName, setStoreName] = useState<string>('');
  const [authorName, setAuthorName] = useState<string>('');
  const [licenseName, setLicenseName] = useState<string>('');
  const [tagsString, setTagsString] = useState<string>('');
  const [projectsString, setProjectsString] = useState<string>('');

  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isUpdating, setIsUpdating] = useState<boolean>(false);

  // Load existing asset details
  useEffect(() => {
    const fetchAsset = async () => {
      setIsLoading(true);
      try {
        const asset = await FarmApiClient.getAssetDetails(assetId);
        if (asset) {
          setAssetName(asset.assetName);
          setLink(asset.link || '');
          setStoreName(asset.storeName || '');
          setAuthorName(asset.authorName || '');
          setLicenseName(asset.licenseName || '');
          setTagsString(asset.tags.join(', '));
          setProjectsString(asset.projects.join(', '));
        } else {
          onMessage('Asset not found for editing.', true);
          onCancel(); // Go back if asset not found
        }
      } catch (e: any) {
        onMessage(`Failed to load asset for editing: ${e.message}`, true);
        onCancel();
      } finally {
        setIsLoading(false);
      }
    };
    fetchAsset();
  }, [assetId, onMessage, onCancel]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsUpdating(true);
    try {
      const request: UpdateAssetRequest = {
        assetName,
        link: link.trim() === '' ? null : link.trim(),
        storeName: storeName.trim() === '' ? null : storeName.trim(),
        authorName: authorName.trim() === '' ? null : authorName.trim(),
        licenseName: licenseName.trim() === '' ? null : licenseName.trim(),
        tagsString: tagsString.trim() === '' ? null : tagsString.trim(),
        projectsString: projectsString.trim() === '' ? null : projectsString.trim(),
      };
      const responseMessage = await FarmApiClient.updateAsset(assetId, request);
      onUpdateSuccess(assetId, responseMessage);
    } catch (e: any) {
      onMessage(`Update failed: ${e.message}`, true);
    } finally {
      setIsUpdating(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="animate-spin text-blue-500" size={48} />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h2 className="text-3xl font-bold text-gray-800 mb-4">Edit Asset (ID: {assetId})</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
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
          <label htmlFor="tagsString" className="block text-sm font-medium text-gray-700 mb-1">
            Tags (comma-separated)
          </label>
          <input
            type="text"
            id="tagsString"
            value={tagsString}
            onChange={(e) => setTagsString(e.target.value)}
            placeholder="e.g., 2D, character, pixelart"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
          />
        </div>

        <div>
          <label htmlFor="projectsString" className="block text-sm font-medium text-gray-700 mb-1">
            Projects (comma-separated)
          </label>
          <input
            type="text"
            id="projectsString"
            value={projectsString}
            onChange={(e) => setProjectsString(e.target.value)}
            placeholder="e.g., MyGameTitle, RPG"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
          />
        </div>

        <div className="flex justify-between mt-6">
          <button
            type="button"
            onClick={onCancel}
            className="px-5 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-opacity-50 flex items-center space-x-2 transition duration-200"
            disabled={isUpdating}
          >
            <XCircle size={18} />
            <span>Cancel</span>
          </button>
          <button
            type="submit"
            className="px-5 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-50 flex items-center space-x-2 transition duration-200"
            disabled={isUpdating}
          >
            {isUpdating ? <Loader2 className="animate-spin" size={18} /> : <Save size={18} />}
            <span>{isUpdating ? 'Updating...' : 'Update Asset'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};