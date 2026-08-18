import React, { useState, useEffect } from 'react';
import {
  format,
  addMonths,
  subMonths,
  startOfMonth,
  endOfMonth,
  startOfWeek,
  endOfWeek,
  eachDayOfInterval,
  isSameMonth,
  isSameDay,
  isBefore,
  isAfter,
  parseISO,
  isWithinInterval,
} from 'date-fns';
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon, CheckCircle2, XCircle } from 'lucide-react';
import { borrowService, DateRange } from '../services/borrowService';

interface AvailabilityCalendarProps {
  itemId: number;
  minDays?: number;
  maxDays?: number;
  onSelectRange: (startDate: string | null, endDate: string | null) => void;
}

export const AvailabilityCalendar: React.FC<AvailabilityCalendarProps> = ({
  itemId,
  minDays = 1,
  maxDays = 14,
  onSelectRange,
}) => {
  const [currentMonth, setCurrentMonth] = useState<Date>(new Date());
  const [bookedRanges, setBookedRanges] = useState<DateRange[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedStart, setSelectedStart] = useState<Date | null>(null);
  const [selectedEnd, setSelectedEnd] = useState<Date | null>(null);

  useEffect(() => {
    setLoading(true);
    borrowService
      .getBookedDateRanges(itemId)
      .then((ranges) => {
        setBookedRanges(ranges);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [itemId]);

  const nextMonth = () => setCurrentMonth(addMonths(currentMonth, 1));
  const prevMonth = () => setCurrentMonth(subMonths(currentMonth, 1));

  const isDateBooked = (date: Date): boolean => {
    return bookedRanges.some((range) => {
      const start = parseISO(range.startDate);
      const end = parseISO(range.endDate);
      return isWithinInterval(date, { start, end }) || isSameDay(date, start) || isSameDay(date, end);
    });
  };

  const handleDateClick = (day: Date) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (isBefore(day, today) || isDateBooked(day)) {
      return;
    }

    if (!selectedStart || (selectedStart && selectedEnd)) {
      setSelectedStart(day);
      setSelectedEnd(null);
      onSelectRange(format(day, 'yyyy-MM-dd'), null);
    } else if (selectedStart && !selectedEnd) {
      if (isBefore(day, selectedStart)) {
        setSelectedStart(day);
        setSelectedEnd(null);
        onSelectRange(format(day, 'yyyy-MM-dd'), null);
      } else {
        // Check if any booked date lies between selectedStart and day
        const daysInBetween = eachDayOfInterval({ start: selectedStart, end: day });
        const hasConflict = daysInBetween.some(isDateBooked);

        if (hasConflict) {
          // Reset to this new start
          setSelectedStart(day);
          setSelectedEnd(null);
          onSelectRange(format(day, 'yyyy-MM-dd'), null);
        } else {
          setSelectedEnd(day);
          onSelectRange(format(selectedStart, 'yyyy-MM-dd'), format(day, 'yyyy-MM-dd'));
        }
      }
    }
  };

  const monthStart = startOfMonth(currentMonth);
  const monthEnd = endOfMonth(monthStart);
  const startDate = startOfWeek(monthStart);
  const endDate = endOfWeek(monthEnd);

  const days = eachDayOfInterval({ start: startDate, end: endDate });
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 select-none">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <CalendarIcon className="w-5 h-5 text-emerald-400" />
          <h4 className="font-bold text-white text-base">
            {format(currentMonth, 'MMMM yyyy')}
          </h4>
        </div>
        <div className="flex items-center gap-1">
          <button
            onClick={prevMonth}
            disabled={isSameMonth(currentMonth, new Date())}
            className="p-1.5 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
          <button
            onClick={nextMonth}
            className="p-1.5 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white transition-colors"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Weekday headers */}
      <div className="grid grid-cols-7 gap-1 text-center text-xs font-semibold text-slate-500 mb-2">
        {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map((d) => (
          <div key={d} className="py-1">
            {d}
          </div>
        ))}
      </div>

      {/* Days grid */}
      <div className="grid grid-cols-7 gap-1">
        {days.map((day) => {
          const isCurrentMonth = isSameMonth(day, currentMonth);
          const isPast = isBefore(day, today);
          const isBooked = isDateBooked(day);
          const isStart = selectedStart && isSameDay(day, selectedStart);
          const isEnd = selectedEnd && isSameDay(day, selectedEnd);
          const isInRange =
            selectedStart &&
            selectedEnd &&
            isAfter(day, selectedStart) &&
            isBefore(day, selectedEnd);

          const isDisabled = !isCurrentMonth || isPast || isBooked;

          let dayClasses = 'h-9 w-full rounded-lg text-xs font-medium flex items-center justify-center transition-all ';

          if (isStart || isEnd) {
            dayClasses += 'bg-emerald-500 text-slate-950 font-bold shadow-md shadow-emerald-500/30 scale-105 z-10';
          } else if (isInRange) {
            dayClasses += 'bg-emerald-950/60 text-emerald-300 border-y border-emerald-500/20';
          } else if (isBooked) {
            dayClasses += 'bg-rose-950/40 text-rose-400/60 line-through cursor-not-allowed';
          } else if (isPast || !isCurrentMonth) {
            dayClasses += 'text-slate-600 opacity-40 cursor-not-allowed';
          } else {
            dayClasses += 'text-slate-200 hover:bg-slate-800 hover:text-emerald-400 cursor-pointer';
          }

          return (
            <button
              key={day.toISOString()}
              onClick={() => handleDateClick(day)}
              disabled={isDisabled}
              className={dayClasses}
            >
              {format(day, 'd')}
            </button>
          );
        })}
      </div>

      {/* Legend & rules */}
      <div className="mt-4 pt-4 border-t border-slate-800/80 flex flex-wrap items-center justify-between gap-3 text-xs">
        <div className="flex items-center gap-4 text-slate-400">
          <div className="flex items-center gap-1.5">
            <div className="w-3 h-3 rounded bg-emerald-500" />
            <span>Selected</span>
          </div>
          <div className="flex items-center gap-1.5">
            <div className="w-3 h-3 rounded bg-rose-950/60 border border-rose-500/30" />
            <span>Booked</span>
          </div>
          <div className="flex items-center gap-1.5">
            <div className="w-3 h-3 rounded bg-slate-800" />
            <span>Available</span>
          </div>
        </div>

        <div className="text-[11px] text-slate-400">
          Duration: <span className="text-emerald-400 font-semibold">{minDays} to {maxDays} days</span>
        </div>
      </div>
    </div>
  );
};
