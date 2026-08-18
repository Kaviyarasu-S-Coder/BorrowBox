import React from 'react';
import { Star } from 'lucide-react';

interface RatingStarsProps {
  rating: number;
  count?: number;
  size?: 'sm' | 'md' | 'lg';
  showNumber?: boolean;
}

export const RatingStars: React.FC<RatingStarsProps> = ({
  rating,
  count,
  size = 'sm',
  showNumber = true,
}) => {
  const starSizes = {
    sm: 'w-3.5 h-3.5',
    md: 'w-4 h-4',
    lg: 'w-5 h-5',
  };

  const textSizes = {
    sm: 'text-xs',
    md: 'text-sm',
    lg: 'text-base',
  };

  return (
    <div className="flex items-center gap-1.5">
      <div className="flex items-center text-amber-400">
        {[1, 2, 3, 4, 5].map((star) => {
          const filled = star <= Math.round(rating);
          return (
            <Star
              key={star}
              className={`${starSizes[size]} ${
                filled ? 'fill-amber-400 text-amber-400' : 'text-slate-600'
              }`}
            />
          );
        })}
      </div>
      {showNumber && (
        <span className={`font-medium text-slate-300 ${textSizes[size]}`}>
          {rating ? rating.toFixed(1) : '5.0'}
        </span>
      )}
      {count !== undefined && (
        <span className={`text-slate-500 ${textSizes[size]}`}>
          ({count})
        </span>
      )}
    </div>
  );
};
