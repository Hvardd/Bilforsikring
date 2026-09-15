import { useEffect, useRef, useState, type KeyboardEvent } from 'react'
import './BonusSelect.css'

type Props = {
  value: string
  disabled: boolean
  invalid?: boolean
  onChange: (value: string) => void
  onBlur: () => void
}

const groups = Array.from({ length: 9 }, (_, index) => index * 10)
const groupLabel = (start: number) =>
  start === 80 ? '80 %' : `${start}–${start + 9} %`

function updateScrollGlow(menu: HTMLDivElement) {
  const dropdown = menu.parentElement
  if (!dropdown) return
  dropdown.dataset.glowTop = String(menu.scrollTop > 1)
  dropdown.dataset.glowBottom = String(
    menu.scrollHeight - menu.clientHeight - menu.scrollTop > 1,
  )
}

export default function BonusSelect({
  value,
  disabled,
  invalid,
  onChange,
  onBlur,
}: Props) {
  const [open, setOpen] = useState(false)
  const [active, setActive] = useState(0)
  const [group, setGroup] = useState<number | null>(null)
  const root = useRef<HTMLDivElement>(null)
  const trigger = useRef<HTMLButtonElement>(null)
  const list = useRef<HTMLDivElement>(null)
  const typed = useRef({ text: '', time: 0 })
  const expanded = open && !disabled
  const options =
    group === null
      ? groups
      : Array.from(
          { length: Math.min(10, 81 - group) },
          (_, index) => group + index,
        )

  useEffect(() => {
    if (!expanded) return
    function dismiss(event: PointerEvent) {
      if (
        event.target instanceof Node &&
        !root.current?.contains(event.target)
      ) {
        setOpen(false)
        onBlur()
      }
    }
    document.addEventListener('pointerdown', dismiss)
    return () => document.removeEventListener('pointerdown', dismiss)
  }, [expanded, onBlur])

  useEffect(() => {
    const menu = list.current
    const option = menu?.children[active]
    if (expanded && menu && option) {
      const menuBounds = menu.getBoundingClientRect()
      const optionBounds = option.getBoundingClientRect()
      if (optionBounds.top < menuBounds.top) {
        menu.scrollTop += optionBounds.top - menuBounds.top
      } else if (optionBounds.bottom > menuBounds.bottom) {
        menu.scrollTop += optionBounds.bottom - menuBounds.bottom
      }
      updateScrollGlow(menu)
    }
  }, [active, expanded, group])

  useEffect(() => {
    const menu = list.current
    if (!expanded || !menu) return
    const observer = new ResizeObserver(() => updateScrollGlow(menu))
    observer.observe(menu)
    return () => observer.disconnect()
  }, [expanded])

  function show() {
    setGroup(null)
    setActive(value === '' ? 0 : Math.floor(Number(value) / 10))
    typed.current = { text: '', time: 0 }
    setOpen(true)
  }

  function choose(bonus: number) {
    trigger.current?.focus({ preventScroll: true })
    if (group === null) {
      setGroup(bonus)
      setActive(
        value !== '' && Number(value) >= bonus && Number(value) <= bonus + 9
          ? Number(value) - bonus
          : 0,
      )
      if (list.current) list.current.scrollTop = 0
      typed.current = { text: '', time: 0 }
      return
    }
    onChange(String(bonus))
    setOpen(false)
    trigger.current?.focus({ preventScroll: true })
  }

  function back() {
    trigger.current?.focus({ preventScroll: true })
    setActive(Math.floor((group ?? 0) / 10))
    setGroup(null)
    typed.current = { text: '', time: 0 }
  }

  function handleKey(event: KeyboardEvent) {
    if (disabled) return
    if (event.key === 'Tab') {
      setOpen(false)
      return
    }
    if (
      event.key === 'Escape' ||
      (event.key === 'ArrowLeft' && group !== null)
    ) {
      if (expanded) {
        event.preventDefault()
        if (group !== null) back()
        else setOpen(false)
        trigger.current?.focus({ preventScroll: true })
      }
      return
    }
    if (
      event.target instanceof HTMLElement &&
      event.target.closest('.bonus-select__back')
    )
      return
    if (
      ['ArrowDown', 'ArrowUp', 'Home', 'End', 'Enter', ' '].includes(event.key)
    ) {
      event.preventDefault()
      if (!expanded) {
        show()
      } else if (event.key === 'Enter' || event.key === ' ') {
        choose(options[active])
      } else {
        setActive(
          event.key === 'Home'
            ? 0
            : event.key === 'End'
              ? options.length - 1
              : Math.max(
                  0,
                  Math.min(
                    options.length - 1,
                    active + (event.key === 'ArrowDown' ? 1 : -1),
                  ),
                ),
        )
      }
    } else if (/^\d$/.test(event.key)) {
      event.preventDefault()
      const now = event.timeStamp
      const text =
        (now - typed.current.time < 700 ? typed.current.text : '') + event.key
      const match = options.findIndex((bonus) => String(bonus).startsWith(text))
      typed.current = { text, time: now }
      if (match !== -1) {
        setActive(match)
        setOpen(true)
      }
    }
  }

  return (
    <div
      ref={root}
      className="bonus-select"
      onKeyDown={handleKey}
      onBlur={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget)) {
          setOpen(false)
          onBlur()
        }
      }}
    >
      <button
        ref={trigger}
        type="button"
        id="bonus"
        name="bonus"
        className="bonus-select__trigger"
        data-placeholder={value === ''}
        role="combobox"
        disabled={disabled}
        aria-labelledby="bonus-label"
        aria-expanded={expanded}
        aria-haspopup="listbox"
        aria-controls="bonus-options"
        aria-required="true"
        aria-invalid={invalid || undefined}
        aria-describedby={invalid ? 'bonus-error' : undefined}
        aria-activedescendant={expanded ? `bonus-option-${active}` : undefined}
        onClick={() => {
          if (expanded) setOpen(false)
          else show()
        }}
      >
        {value === '' ? 'Velg bonus' : `${value} %`}
      </button>
      <div
        className="bonus-select__dropdown bonus-select__dropdown--fixed-height"
        hidden={!expanded}
      >
        {group !== null && (
          <button
            type="button"
            className="bonus-select__back"
            onClick={back}
            // Keep focus inside the menu so Safari does not dismiss it before click.
            onMouseDown={(event) => event.preventDefault()}
            aria-label={`Tilbake til bonusgrupper fra ${groupLabel(group)}`}
          >
            <span aria-hidden="true">‹</span> {groupLabel(group)}
          </button>
        )}
        <div
          ref={list}
          id="bonus-options"
          role="listbox"
          aria-label={
            group === null
              ? 'Velg bonusgruppe'
              : `Velg bonus: ${groupLabel(group)}`
          }
          className="bonus-select__options"
          onScroll={(event) => updateScrollGlow(event.currentTarget)}
        >
          {options.map((bonus, index) => (
            <div
              key={`${group}-${bonus}`}
              id={`bonus-option-${index}`}
              role="option"
              tabIndex={-1}
              aria-selected={
                group === null
                  ? value !== '' &&
                    Math.floor(Number(value) / 10) * 10 === bonus
                  : value === String(bonus)
              }
              className={`bonus-select__option${active === index ? ' bonus-select__option--active' : ''}`}
              onClick={() => choose(bonus)}
            >
              <span>{group === null ? groupLabel(bonus) : `${bonus} %`}</span>
              {group === null ? (
                <span aria-hidden="true">›</span>
              ) : (
                value === String(bonus) && <span aria-hidden="true">✓</span>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
