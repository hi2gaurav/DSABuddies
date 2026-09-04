import React, { useMemo } from 'react';
import { clsx } from 'clsx';

interface ActivityHeatmapProps {
  data: Record<string, number>; // date "YYYY-MM-DD" -> count
}

const ActivityHeatmap: React.FC<ActivityHeatmapProps> = ({ data }) => {
  // Generate last 6 months of dates
  const { weeks, monthLabels, maxCount: _maxCount } = useMemo(() => {
    const today = new Date();
    // Start from 6 months ago, aligned to Sunday
    const startDate = new Date(today);
    startDate.setMonth(today.getMonth() - 6);
    startDate.setDate(startDate.getDate() - startDate.getDay()); // Go back to Sunday

    const weeksList = [];
    let currentWeek = [];
    const months = [];
    let lastMonth = -1;

    let maxC = 0;
    Object.values(data).forEach(v => { if (v > maxC) maxC = v; });
    // Normalize max to at least 4 for coloring scale
    if (maxC < 4) maxC = 4;

    const currDate = new Date(startDate);
    
    while (currDate <= today || currentWeek.length > 0) {
      if (currentWeek.length === 7) {
        weeksList.push(currentWeek);
        currentWeek = [];
      }
      
      // Stop if we've passed today and finished the week
      if (currDate > today && currentWeek.length === 0) break;

      const dateStr = currDate.toISOString().split('T')[0];
      const count = data[dateStr] || 0;
      
      // Track months for labels
      if (currDate.getMonth() !== lastMonth && currDate.getDate() < 15) {
        months.push({
          month: currDate.toLocaleString('default', { month: 'short' }),
          index: weeksList.length
        });
        lastMonth = currDate.getMonth();
      }

      currentWeek.push({
        date: dateStr,
        count,
        isFuture: currDate > today
      });

      currDate.setDate(currDate.getDate() + 1);
    }
    
    if (currentWeek.length > 0) {
      while(currentWeek.length < 7) {
        currentWeek.push({ date: '', count: 0, isFuture: true });
      }
      weeksList.push(currentWeek);
    }

    return { weeks: weeksList, monthLabels: months, maxCount: maxC };
  }, [data]);

  const getColorClass = (count: number, isFuture: boolean) => {
    if (isFuture) return 'bg-transparent';
    if (count === 0) return 'bg-gray-100 dark:bg-slate-800';
    if (count === 1) return 'bg-emerald-200 dark:bg-emerald-900';
    if (count === 2) return 'bg-emerald-300 dark:bg-emerald-700';
    if (count === 3) return 'bg-emerald-400 dark:bg-emerald-500';
    return 'bg-emerald-500 dark:bg-emerald-400';
  };

  return (
    <div className="w-full">
      <div className="flex text-xs text-gray-500 dark:text-gray-400 mb-2 relative h-4">
        {monthLabels.map((m, i) => (
          <span 
            key={i} 
            className="absolute" 
            style={{ left: `${(m.index / weeks.length) * 100}%` }}
          >
            {m.month}
          </span>
        ))}
      </div>
      
      <div className="flex gap-1">
        <div className="flex flex-col gap-1 text-xs text-gray-500 dark:text-gray-400 pr-2 pt-2">
          <span className="h-3 leading-3">Mon</span>
          <span className="h-3 leading-3 opacity-0">Tue</span>
          <span className="h-3 leading-3">Wed</span>
          <span className="h-3 leading-3 opacity-0">Thu</span>
          <span className="h-3 leading-3">Fri</span>
          <span className="h-3 leading-3 opacity-0">Sat</span>
          <span className="h-3 leading-3 opacity-0">Sun</span>
        </div>
        
        <div className="flex gap-1">
          {weeks.map((week, wi) => (
            <div key={wi} className="flex flex-col gap-1 pt-1">
              {week.map((day, di) => (
                <div 
                  key={di} 
                  title={day.isFuture ? '' : `${day.count} contributions on ${day.date}`}
                  className={clsx(
                    "w-3 h-3 rounded-sm transition-colors",
                    getColorClass(day.count, day.isFuture),
                    !day.isFuture && "hover:ring-1 hover:ring-black dark:hover:ring-white cursor-help"
                  )}
                />
              ))}
            </div>
          ))}
        </div>
      </div>
      
      <div className="mt-4 flex items-center justify-end gap-2 text-xs text-gray-500 dark:text-gray-400">
        <span>Less</span>
        <div className="flex gap-1">
          <div className="w-3 h-3 rounded-sm bg-gray-100 dark:bg-slate-800" />
          <div className="w-3 h-3 rounded-sm bg-emerald-200 dark:bg-emerald-900" />
          <div className="w-3 h-3 rounded-sm bg-emerald-300 dark:bg-emerald-700" />
          <div className="w-3 h-3 rounded-sm bg-emerald-400 dark:bg-emerald-500" />
          <div className="w-3 h-3 rounded-sm bg-emerald-500 dark:bg-emerald-400" />
        </div>
        <span>More</span>
      </div>
    </div>
  );
};

export default ActivityHeatmap;
