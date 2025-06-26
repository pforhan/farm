import React, { useState, useEffect } from 'react';
import { Asset, FileDetail } from '../types/models';
import { FarmApiClient } from '../api/farmApiClient';
import { Loader2, ArrowLeft, Edit, Download, Image as ImageIcon, Music, FileText } from 'lucide-react'; // Icons

interface AssetDetailsScreenProps {
  assetId: number;
  onBackClick: () => void;
  onEditClick: (assetId: number) => void;
  onMessage: (msg: string, isError?: boolean) => void;
}

export const AssetDetailsScreen: React.FC<AssetDetailsScreenProps> = ({
  assetId,
  onBackClick,
  onEditClick,
  onMessage,
}) => {
  const [asset, setAsset] = useState<Asset | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchAssetDetails = async () => {
      setIsLoading(true);
      try {
        const fetchedAsset = await FarmApiClient.getAssetDetails(assetId);
        setAsset(fetchedAsset);
      } catch (e: any) {
        onMessage(`Failed to load asset details: ${e.message}`, true);
        setAsset(null);
      } finally {
        setIsLoading(false);
      }
    };

    fetchAssetDetails();
  }, [assetId, onMessage]);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="animate-spin text-blue-500" size={48} />
      </div>
    );
  }

  if (asset === null) {
    return (
      <div className="text-center p-4">
        <p className="text-lg text-red-600">Asset not found.</p>
        <button
          onClick={onBackClick}
          className="mt-4 px-4 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-opacity-50 flex items-center justify-center space-x-2 mx-auto transition duration-200"
        >
          <ArrowLeft size={18} />
          <span>Back to Browse</span>
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h2 className="text-3xl font-bold text-gray-800">
        Asset: {asset.assetName} <span className="text-xl font-normal text-gray-500">(ID: {asset.assetId})</span>
      </h2>

      {asset.previewThumbnail && (
        <div className="flex justify-center bg-gray-50 p-4 rounded-lg">
          <img
            src={asset.previewThumbnail}
            alt="Asset Preview"
            className="max-w-full h-64 object-contain rounded-lg shadow-md"
            onError={(e) => {
              e.currentTarget.onerror = null; // Prevent infinite loop
              e.currentTarget.src = `https://placehold.co/256x256/E2E8F0/64748B?text=No+Preview`;
            }}
          />
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-gray-700">
        <p>
          <strong className="font-semibold">Store:</strong> {asset.storeName || 'N/A'}
        </p>
        <p>
          <strong className="font-semibold">Author:</strong> {asset.authorName || 'N/A'}
        </p>
        <p>
          <strong className="font-semibold">License:</strong> {asset.licenseName || 'N/A'}
        </p>
        <p>
          <strong className="font-semibold">Link:</strong>{' '}
          {asset.link ? (
            <a href={asset.link} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline">
              {asset.link}
            </a>
          ) : (
            'N/A'
          )}
        </p>
        <p className="md:col-span-2">
          <strong className="font-semibold">Tags:</strong> {asset.tags.join(', ') || 'None'}
        </p>
        <p className="md:col-span-2">
          <strong className="font-semibold">Projects:</strong> {asset.projects.join(', ') || 'None'}
        </p>
      </div>

      <h3 className="text-2xl font-bold text-gray-800 mt-8 mb-4">Files:</h3>
      {asset.files.length === 0 ? (
        <p className="text-gray-600 italic">No files associated with this asset.</p>
      ) : (
        <div className="space-y-4">
          {asset.files.map((file) => (
            <FileDetailCard key={file.fileId} file={file} />
          ))}
        </div>
      )}

      <div className="flex justify-between mt-8">
        <button
          onClick={onBackClick}
          className="px-5 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-opacity-50 flex items-center space-x-2 transition duration-200"
        >
          <ArrowLeft size={18} />
          <span>Back to Browse</span>
        </button>
        <button
          onClick={() => onEditClick(asset.assetId)}
          className="px-5 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 flex items-center space-x-2 transition duration-200"
        >
          <Edit size={18} />
          <span>Edit Asset</span>
        </button>
      </div>
    </div>
  );
};

interface FileDetailCardProps {
  file: FileDetail;
}

const FileDetailCard: React.FC<FileDetailCardProps> = ({ file }) => {
  const isImage = file.fileType.startsWith('image/');
  const isAudio = file.fileType.startsWith('audio/');
  const isText = file.fileType.startsWith('text/') || file.fileType.includes('json') || file.fileType.includes('xml');

  // Simple placeholder for broken image links
  const handleImageError = (e: React.SyntheticEvent<HTMLImageElement, Event>) => {
    e.currentTarget.onerror = null; // Prevent infinite loop
    e.currentTarget.src = `https://placehold.co/150x100/E2E8F0/64748B?text=No+Preview`;
  };

  return (
    <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 space-y-2">
      <p className="text-lg font-semibold text-gray-800">{file.fileName}</p>
      <p className="text-sm text-gray-600">
        Type: {file.fileType} | Size: {(file.fileSize / (1024 * 1024)).toFixed(2)} MB
      </p>

      {file.previewPath && isImage && (
        <div className="flex justify-center py-2">
          <img
            src={file.previewPath}
            alt={`${file.fileName} preview`}
            className="max-h-40 w-auto object-contain rounded-md"
            onError={handleImageError}
          />
        </div>
      )}

      {isAudio && (
        <div className="py-2">
          <audio controls src={file.publicPath} className="w-full"></audio>
        </div>
      )}

      <a
        href={file.publicPath}
        download={file.fileName}
        className="inline-flex items-center space-x-2 px-3 py-1 bg-blue-100 text-blue-700 rounded-md hover:bg-blue-200 transition duration-200"
      >
        <Download size={16} />
        <span>Download File</span>
      </a>

      {isImage && <ImageIcon size={20} className="text-purple-500 inline-block ml-2" />}
      {isAudio && <Music size={20} className="text-purple-500 inline-block ml-2" />}
      {isText && <FileText size={20} className="text-purple-500 inline-block ml-2" />}
    </div>
  );
};