import React, { useState, useEffect } from 'react';
import { Asset } from '../types/models';
import { FarmApiClient } from '../api/farmApiClient';
import { Loader2, Eye, Edit } from 'lucide-react'; // Icons

interface BrowseAssetsProps {
  onAssetClick: (assetId: number) => void;
  onEditClick: (assetId: number) => void;
  onMessage: (msg: string, isError?: boolean) => void;
}

export const BrowseAssets: React.FC<BrowseAssetsProps> = ({ onAssetClick, onEditClick, onMessage }) => {
  const [assets, setAssets] = useState<Asset[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchAssets = async () => {
      setIsLoading(true);
      try {
        const fetchedAssets = await FarmApiClient.getAssets();
        setAssets(fetchedAssets);
      } catch (e: any) {
        onMessage(`Failed to load assets: ${e.message}`, true);
      } finally {
        setIsLoading(false);
      }
    };

    fetchAssets();
  }, [onMessage]);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="animate-spin text-blue-500" size={48} />
      </div>
    );
  }

  if (assets.length === 0) {
    return (
      <div className="text-center p-4">
        <p className="text-lg text-gray-600">No assets found. Start by uploading one!</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h2 className="text-3xl font-bold text-gray-800 mb-4">Browse Assets</h2>
      <div className="overflow-x-auto rounded-lg shadow-md">
        <table className="min-w-full bg-white border-collapse">
          <thead>
            <tr className="bg-gray-100 text-gray-600 uppercase text-sm leading-normal">
              <th className="py-3 px-6 text-left">ID</th>
              <th className="py-3 px-6 text-left">Preview</th>
              <th className="py-3 px-6 text-left">Name</th>
              <th className="py-3 px-6 text-left">Link</th>
              <th className="py-3 px-6 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="text-gray-700 text-sm font-light">
            {assets.map((asset) => (
              <tr key={asset.assetId} className="border-b border-gray-200 hover:bg-gray-50">
                <td className="py-3 px-6 text-left whitespace-nowrap">{asset.assetId}</td>
                <td className="py-3 px-6 text-left">
                  {asset.previewThumbnail ? (
                    <img
                      src={asset.previewThumbnail}
                      alt="Preview"
                      className="w-12 h-12 object-cover rounded-md shadow-sm"
                      onError={(e) => {
                        e.currentTarget.onerror = null;
                        e.currentTarget.src = `https://placehold.co/48x48/E2E8F0/64748B?text=N/A`;
                      }}
                    />
                  ) : (
                    <div className="w-12 h-12 bg-gray-200 flex items-center justify-center rounded-md text-xs text-gray-500">N/A</div>
                  )}
                </td>
                <td className="py-3 px-6 text-left">{asset.assetName}</td>
                <td className="py-3 px-6 text-left">
                  {asset.link ? (
                    <a href={asset.link} target="_blank" rel="noopener noreferrer" className="text-blue-500 hover:underline">
                      View Link
                    </a>
                  ) : (
                    'N/A'
                  )}
                </td>
                <td className="py-3 px-6 text-center">
                  <div className="flex item-center justify-center space-x-2">
                    <button
                      onClick={() => onAssetClick(asset.assetId)}
                      className="flex items-center justify-center w-8 h-8 rounded-full bg-blue-100 text-blue-700 hover:bg-blue-200 transition duration-200 tooltip"
                      data-tooltip="Details"
                    >
                      <Eye size={16} />
                    </button>
                    <button
                      onClick={() => onEditClick(asset.assetId)}
                      className="flex items-center justify-center w-8 h-8 rounded-full bg-yellow-100 text-yellow-700 hover:bg-yellow-200 transition duration-200 tooltip"
                      data-tooltip="Edit"
                    >
                      <Edit size={16} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};