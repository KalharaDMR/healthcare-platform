import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import Button from './Button';

export default function PaginationBar({
  page,
  totalPages,
  totalItems,
  pageSize,
  onPageChange,
  itemLabel = 'items',
}) {
  if (totalPages <= 1 && totalItems === 0) return null;

  const from = totalItems === 0 ? 0 : (page - 1) * pageSize + 1;
  const to = Math.min(page * pageSize, totalItems);

  return (
    <div
      style={{
        display: 'flex',
        flexWrap: 'wrap',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: 12,
        marginTop: 20,
        paddingTop: 16,
        borderTop: '1px solid var(--border)',
      }}
    >
      <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
        {totalItems === 0
          ? `No ${itemLabel}`
          : `Showing ${from}–${to} of ${totalItems} ${itemLabel}`}
      </span>
      {totalPages > 1 && (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <Button
            variant="ghost"
            size="sm"
            disabled={page <= 1}
            onClick={() => onPageChange(page - 1)}
            icon={<ChevronLeft size={16} />}
          >
            Prev
          </Button>
          <span style={{ fontSize: '0.875rem', fontWeight: 600, minWidth: 80, textAlign: 'center' }}>
            Page {page} / {totalPages}
          </span>
          <Button
            variant="ghost"
            size="sm"
            disabled={page >= totalPages}
            onClick={() => onPageChange(page + 1)}
            icon={<ChevronRight size={16} />}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
}
