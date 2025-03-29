import React, { useEffect, useState } from 'react';

type Color = 'WHITE' | 'BLACK';
type Piece = 'PAWN' | 'KNIGHT' | 'BISHOP' | 'ROOK' | 'QUEEN' | 'KING';

interface FiltersProps {
  onFiltersChange: (filters: {
    color: Color;
    selectedPieces: Piece[];
    minElo: number;
    maxElo: number;
    limit: number;
  }) => void;
}

const Filters: React.FC<FiltersProps> = ({ onFiltersChange }) => {
  const [color, setColor] = useState<Color>('WHITE');
  const [selectedPieces, setSelectedPieces] = useState<Piece[]>(['PAWN']);
  const [minElo, setMinElo] = useState(1000);
  const [maxElo, setMaxElo] = useState(2000);
  const [limit, setLimit] = useState(12);

  useEffect(() => {
    onFiltersChange({ color, selectedPieces, minElo, maxElo, limit });
  }, [color, selectedPieces, minElo, maxElo, limit, onFiltersChange]);

  const togglePiece = (piece: Piece) => {
    setSelectedPieces((prev) =>
      prev.includes(piece) ? prev.filter((p) => p !== piece) : [...prev, piece]
    );
  };

  const pieceOptions: Piece[] = ['PAWN', 'KNIGHT', 'BISHOP', 'ROOK', 'QUEEN', 'KING'];

  return (
    <div className="mb-6">
      <h2 className="text-xl font-semibold mb-2">Filters</h2>

      {/* Color Selector */}
      <div className="mb-4">
        <p className="font-medium mb-2">Choose color:</p>
        <label className="mr-4">
          <input
            type="radio"
            value="WHITE"
            checked={color === 'WHITE'}
            onChange={() => setColor('WHITE')}
          />
          <span className="ml-2">White</span>
        </label>
        <label>
          <input
            type="radio"
            value="BLACK"
            checked={color === 'BLACK'}
            onChange={() => setColor('BLACK')}
          />
          <span className="ml-2">Black</span>
        </label>
      </div>

      {/* Piece Checkboxes */}
      <div className="mb-4">
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

      {/* Elo Fields */}
      <div className="flex gap-4 mb-4">
        <div>
          <label className="block text-sm font-medium mb-1">Min ELO</label>
          <input
            type="number"
            min={500}
            max={2500}
            value={minElo}
            onChange={(e) => setMinElo(Number(e.target.value))}
            className="w-full p-1 border rounded"
          />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Max ELO</label>
          <input
            type="number"
            min={500}
            max={2500}
            value={maxElo}
            onChange={(e) => setMaxElo(Number(e.target.value))}
            className="w-full p-1 border rounded"
          />
        </div>
      </div>

      {/* Limit Field */}
      <div>
        <label className="block text-sm font-medium mb-1">Number of results (1–30)</label>
        <input
          type="number"
          min={1}
          max={30}
          value={limit}
          onChange={(e) => setLimit(Number(e.target.value))}
          className="w-full p-1 border rounded"
        />
      </div>
    </div>
  );
};

export default Filters;