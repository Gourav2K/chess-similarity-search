import React, { useEffect, useState } from 'react';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Checkbox } from '@/components/ui/checkbox';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent } from '@/components/ui/card';

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
  const [limit, setLimit] = useState(5);

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
    <Card className="p-4">
      <CardContent className="space-y-6">

        {/* Color Selection */}
        <div>
          <Label className="text-base">Choose color:</Label>
          <RadioGroup
            defaultValue={color}
            onValueChange={(val: Color) => setColor(val)}
            className="mt-2"
          >
            <div className="flex gap-4">
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="WHITE" id="white" />
                <Label htmlFor="white">White</Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="BLACK" id="black" />
                <Label htmlFor="black">Black</Label>
              </div>
            </div>
          </RadioGroup>
        </div>

        {/* Piece Checkboxes */}
        <div>
          <Label className="text-base">Pieces to match:</Label>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 mt-2">
            {pieceOptions.map((piece) => (
              <div key={piece} className="flex items-center space-x-2">
                <Checkbox
                  id={piece}
                  checked={selectedPieces.includes(piece)}
                  onCheckedChange={() => togglePiece(piece)}
                />
                <Label htmlFor={piece} className="capitalize">
                  {piece.toLowerCase()}
                </Label>
              </div>
            ))}
          </div>
        </div>

        {/* Elo Range */}
        <div className="flex gap-4">
          <div className="flex-1">
            <Label htmlFor="minElo">Min ELO</Label>
            <Input
              type="number"
              id="minElo"
              min={500}
              max={2500}
              value={minElo}
              onChange={(e) => setMinElo(Number(e.target.value))}
            />
          </div>
          <div className="flex-1">
            <Label htmlFor="maxElo">Max ELO</Label>
            <Input
              type="number"
              id="maxElo"
              min={500}
              max={2500}
              value={maxElo}
              onChange={(e) => setMaxElo(Number(e.target.value))}
            />
          </div>
        </div>

        {/* Result Limit */}
        <div>
          <Label htmlFor="limit">Number of results (1–10)</Label>
          <Input
            type="number"
            id="limit"
            min={1}
            max={10}
            value={limit}
            onChange={(e) => setLimit(Number(e.target.value))}
          />
        </div>
      </CardContent>
    </Card>
  );
};

export default Filters;