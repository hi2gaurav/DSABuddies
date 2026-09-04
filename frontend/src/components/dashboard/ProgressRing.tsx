import React, { useEffect, useState } from 'react';

interface ProgressRingProps {
  completed: number;
  total: number;
  size?: number;
  strokeWidth?: number;
}

const ProgressRing: React.FC<ProgressRingProps> = ({ 
  completed, 
  total, 
  size = 120, 
  strokeWidth = 10 
}) => {
  const [progress, setProgress] = useState(0);
  const percentage = total > 0 ? Math.round((completed / total) * 100) : 0;
  
  const radius = (size - strokeWidth) / 2;
  const circumference = radius * 2 * Math.PI;
  
  useEffect(() => {
    // Animate fill on mount
    const timer = setTimeout(() => {
      setProgress(percentage);
    }, 100);
    return () => clearTimeout(timer);
  }, [percentage]);
  
  const strokeDashoffset = circumference - (progress / 100) * circumference;
  
  return (
    <div className="relative flex flex-col items-center justify-center">
      <svg
        width={size}
        height={size}
        className="transform -rotate-90 transition-all duration-1000 ease-out"
      >
        {/* Background circle */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke="currentColor"
          strokeWidth={strokeWidth}
          fill="transparent"
          className="text-gray-200 dark:text-slate-700"
        />
        {/* Progress circle */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke="currentColor"
          strokeWidth={strokeWidth}
          fill="transparent"
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          strokeLinecap="round"
          className="text-emerald-500 transition-all duration-1000 ease-out"
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span className="text-2xl font-bold dark:text-white">
          {percentage}%
        </span>
        <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
          {completed}/{total}
        </span>
      </div>
    </div>
  );
};

export default ProgressRing;
