import React from 'react';
import { Chessboard } from 'react-chessboard';
import lichessLogo from '../assets/lichess-black.svg';

interface Result {
  similarityScore: number;
  moveNumber: number;
  position: {
    fen: string;
  };
  game: {
    whiteName: string;
    blackName: string;
    result: string;
    site: string;
    blackElo: number;
    whiteElo: number;
  };
}

interface Props {
  results: Result[];
}

const ResultsDisplay: React.FC<Props> = ({ results }) => {
  if (!results || results.length === 0) {
    return <p>No results found.</p>;
  }

  return (
    <div>
      <h2 className="text-xl font-semibold mb-4">Similar Positions</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {results.map((result, index) => (
          <div
          key={index}
          className="border rounded-lg p-4 shadow-sm bg-white min-h-[460px] flex flex-col justify-start transition-transform duration-200 hover:shadow-lg hover:-translate-y-1"
        >
          <div className="w-[320px] mx-auto">
            <Chessboard
              id={`mini-board-${index}`}
              position={result.position.fen}
              arePiecesDraggable={false}
              boardWidth={320}
            />
            <div className="mt-3 text-sm text-gray-800 text-left">
              <p><strong>Similarity:</strong> {(result.similarityScore * 100).toFixed(1)}%</p>
              <p><strong>Move:</strong> {result.moveNumber}</p>
              <p><strong>Game:</strong> {result.game.whiteName} vs {result.game.blackName}</p>
              <p><strong>ELO:</strong> {result.game.whiteElo} - {result.game.blackElo}</p>
              <p><strong>Result:</strong> {result.game.result}</p>
        
              <a
                href={result.game.site}
                target="_blank"
                rel="noopener noreferrer"
                className="mt-3 inline-block"
              >
                <button className="mt-2 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition flex items-center gap-2">
                    <img
                        src={lichessLogo}
                        alt="Lichess"
                        className="w-5 h-5"
                    />
                    View on Lichess
                </button>
              </a>
            </div>
          </div>
        </div>
        ))}
      </div>
    </div>
  );
};

export default ResultsDisplay;
