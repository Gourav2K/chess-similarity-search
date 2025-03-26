import React, { useState } from 'react';

type Color = 'WHITE' | 'BLACK';
type Piece = 'PAWN' | 'KNIGHT' | 'BISHOP' | 'ROOK' | 'QUEEN' | 'KING';

interface FiltersProps {
  onFiltersChange: (filters: { color: Color; selectedPieces: Piece[] }) => void;
}

const Filters: React.FC<FiltersProps> = ({ onFiltersChange }) => {
  const [color, setColor] = useState<Color>('WHITE');
  const [selectedPieces, setSelectedPieces] = useState<Piece[]>([]);

  const togglePiece = (piece: Piece) => {
    const updated = selectedPieces.includes(piece)
      ? selectedPieces.filter((p) => p !== piece)
      : [...selectedPieces, piece];

    setSelectedPieces(updated);
    onFiltersChange({ color, selectedPieces: updated });
  };

  const handleColorChange = (newColor: Color) => {
    setColor(newColor);
    onFiltersChange({ color: newColor, selectedPieces });
  };

  const pieceOptions: Piece[] = ['PAWN', 'KNIGHT', 'BISHOP', 'ROOK', 'QUEEN', 'KING'];

  return (
    <div className="mb-6">
      <h2 className="text-xl font-semibold mb-2">Filters</h2>

      <div className="mb-4">
        <p className="font-medium mb-2">Choose color:</p>
        <label className="mr-4">
          <input
            type="radio"
            value="WHITE"
            checked={color === 'WHITE'}
            onChange={() => handleColorChange('WHITE')}
          />
          <span className="ml-2">White</span>
        </label>
        <label>
          <input
            type="radio"
            value="BLACK"
            checked={color === 'BLACK'}
            onChange={() => handleColorChange('BLACK')}
          />
          <span className="ml-2">Black</span>
        </label>
      </div>

      <div>
        <p className="font-medium mb-2">Pieces to match:</p>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
          {pieceOptions.map((piece) => (
            <label key={piece} className="flex items-center">
              <input
                type="checkbox"
                value={piece}
                checked={selectedPieces.includes(piece)}
                onChange={() => togglePiece(piece)}
              />
              <span className="ml-2 capitalize">{piece.toLowerCase()}</span>
            </label>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Filters;
