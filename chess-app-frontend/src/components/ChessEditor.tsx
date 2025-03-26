import React, { useEffect, useState } from 'react';
import { Chessboard } from 'react-chessboard';
import { Chess } from 'chess.js';

interface ChessEditorProps {
  onFenChange: (fen: string) => void;
}

const ChessEditor: React.FC<ChessEditorProps> = ({ onFenChange }) => {
  const [game] = useState(new Chess());
  const [fen, setFen] = useState(game.fen());
  const [showFenInput, setShowFenInput] = useState(false);

  useEffect(() => {
    onFenChange(fen); // Notify parent whenever FEN changes
  }, [fen, onFenChange]);

  const handlePieceDrop = (sourceSquare: string, targetSquare: string) => {
    const move = game.move({
      from: sourceSquare,
      to: targetSquare,
      promotion: 'q',
    });

    if (move === null) return false;

    const updatedFen = game.fen();
    setFen(updatedFen);
    return true;
  };

  const handleFenInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newFen = e.target.value;
    setFen(newFen);
    try {
      game.load(newFen);
    } catch {
      // Invalid FEN — ignore but still store it
    }
  };

  return (
    <div className="mb-6">
      <h2 className="text-xl font-semibold mb-2">Set up your position</h2>

      <label className="block mb-4">
        <input
          type="checkbox"
          checked={showFenInput}
          onChange={() => setShowFenInput((prev) => !prev)}
          className="mr-2"
        />
        Use FEN input instead of board
      </label>

      {showFenInput ? (
        <input
          type="text"
          value={fen}
          onChange={handleFenInputChange}
          className="w-full p-2 border rounded"
        />
      ) : (
        <Chessboard
          id="EditableBoard"
          position={fen}
          onPieceDrop={handlePieceDrop}
          boardWidth={400}
        />
      )}

      <p className="mt-2 text-sm text-gray-600">Current FEN: {fen}</p>
    </div>
  );
};

export default ChessEditor;