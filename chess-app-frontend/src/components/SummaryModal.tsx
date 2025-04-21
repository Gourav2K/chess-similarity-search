import React from 'react';
import ReactMarkdown from 'react-markdown';
import { Chessboard } from 'react-chessboard';
import lichessLogo from '../assets/lichess-black.svg';
import copyIcon from '../assets/copy-solid.svg';
import searchLogo from '../assets/magnifying-glass-solid.svg'
import remarkGfm from 'remark-gfm';

interface SummaryModalProps {
    isOpen: boolean;
    onClose: () => void;
    summary: string;
    gameId: string;
    positionFen: string;
    gameInfo: {
      whiteName: string;
      blackName: string;
      whiteElo: number;
      blackElo: number;
      result: string;
      site: string;
    };
  }
  
  const SummaryModal: React.FC<SummaryModalProps> = ({ isOpen, onClose, summary, gameId, positionFen, gameInfo }) => {
    if (!isOpen) return null;
  
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 overflow-y-auto">
        <div className="bg-white rounded-lg p-6 w-full max-w-2xl shadow-xl relative max-h-[90vh] overflow-y-auto">
          <button
            onClick={onClose}
            className="absolute top-2 right-2 text-gray-600 hover:text-black text-2xl"
          >
            &times;
          </button>
  
          <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
            <img src={searchLogo} alt="Lichess" className="w-5 h-5" />
             Strategy Overview — {gameInfo.whiteName} vs {gameInfo.blackName}
          </h2>
  
          <div className="mb-4 flex flex-col items-center">
            <Chessboard position={positionFen} boardWidth={320} arePiecesDraggable={false} />
            <p className="mt-2 text-sm text-gray-700">
              <strong>{gameInfo.whiteName}</strong> ({gameInfo.whiteElo}) vs <strong>{gameInfo.blackName}</strong> ({gameInfo.blackElo})
            </p>
            <p className="text-sm text-gray-600"><strong>Result:</strong> {gameInfo.result}</p>
          </div>
  
          <div className="prose prose-sm text-gray-800 max-w-none bg-gray-100 p-4 rounded max-h-[60vh] overflow-y-auto">
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {summary}
            </ReactMarkdown>
          </div>
  
          <div className="text-right mt-4 flex justify-end gap-3">
            <button
              onClick={() => navigator.clipboard.writeText(summary)}
              className="px-4 py-2 bg-gray-700 text-white rounded hover:bg-gray-800 flex items-center gap-2"
            >
              <img
                src={copyIcon}
                alt="Copy"
                className="w-5 h-5"
              />
              Copy Summary
            </button>
            <a
              href={gameInfo.site}
              target="_blank"
              rel="noopener noreferrer"
            >
              <button className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 flex items-center gap-2">
                <img
                  src={lichessLogo}
                  alt="Lichess"
                  className="w-6 h-6"
                />
                View on Lichess
              </button>
            </a>
          </div>
        </div>
      </div>
    );
  };
  
  export default SummaryModal;