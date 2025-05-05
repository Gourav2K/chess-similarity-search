import React from 'react';
import ReactMarkdown from 'react-markdown';
import { Chessboard } from 'react-chessboard';
import lichessLogo from '../assets/lichess-black.svg';
import copyIcon from '../assets/copy-solid.svg';
import searchLogo from '../assets/magnifying-glass-solid.svg';
import remarkGfm from 'remark-gfm';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';

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

const SummaryModal: React.FC<SummaryModalProps> = ({
  isOpen,
  onClose,
  summary,
  gameId,
  positionFen,
  gameInfo,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 overflow-y-auto">
      <Card className="w-full max-w-2xl relative max-h-[90vh] overflow-y-auto">
        <CardContent className="p-6">
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
            <Chessboard
              position={positionFen}
              boardWidth={320}
              arePiecesDraggable={false}
            />
            <p className="mt-2 text-sm text-gray-700">
              <strong>{gameInfo.whiteName}</strong> ({gameInfo.whiteElo}) vs{' '}
              <strong>{gameInfo.blackName}</strong> ({gameInfo.blackElo})
            </p>
            <p className="text-sm text-gray-600">
              <strong>Result:</strong> {gameInfo.result}
            </p>
          </div>

          <div className="prose prose-sm text-gray-800 max-w-none">
            <ReactMarkdown remarkPlugins={[remarkGfm]}>{summary}</ReactMarkdown>
          </div>

          <div className="text-right mt-4 flex justify-end gap-3">
          <Button
            onClick={() => navigator.clipboard.writeText(summary)}
            className="bg-gray-700 hover:bg-gray-800 text-white"
            >
            <img src={copyIcon} alt="Copy" className="w-5 h-5" />
            Copy Summary
            </Button>
            <a href={gameInfo.site} target="_blank" rel="noopener noreferrer">
              <Button className="bg-blue-600 hover:bg-blue-700">
                <img src={lichessLogo} alt="Lichess" className="w-6 h-6" />
                View on Lichess
              </Button>
            </a>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default SummaryModal;
