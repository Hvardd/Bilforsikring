import { useEffect, useRef, useState, type KeyboardEvent } from 'react'
import { insuranceTypes } from './insurancePurchaseValidation'
import './BonusSelect.css'

type Props = {
  value: string
  disabled: boolean
  invalid?: boolean
  onChange: (value: string) => void
  onBlur: () => void
}

const options = Object.entries(insuranceTypes)

export default function InsuranceTypeSelect({
  value,
  disabled,
  invalid,
  onChange,
  onBlur,
}: Props) {
  const [open, setOpen] = useState(false)
  const [active, setActive] = useState(0)
  const root = useRef<HTMLDivElement>(null)
  const trigger = useRef<HTMLButtonElement>(null)
  const expanded = open && !disabled

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

  function show() {
    setActive(
      Math.max(
        0,
        options.findIndex(([type]) => type === value),
      ),
    )
    setOpen(true)
  }

  function choose(type: string) {
    onChange(type)
    setOpen(false)
    trigger.current?.focus({ preventScroll: true })
  }

  function handleKey(event: KeyboardEvent) {
    if (disabled) return
    if (event.key === 'Tab' || event.key === 'Escape') {
      if (event.key === 'Escape' && expanded) event.preventDefault()
      setOpen(false)
      return
    }
    if (
      ['ArrowDown', 'ArrowUp', 'Home', 'End', 'Enter', ' '].includes(event.key)
    ) {
      event.preventDefault()
      if (!expanded) show()
      else if (event.key === 'Enter' || event.key === ' ')
        choose(options[active][0])
      else
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
    } else if (event.key.length === 1) {
      const match = options.findIndex(([, label]) =>
        label.toLowerCase().startsWith(event.key.toLowerCase()),
      )
      if (match >= 0) {
        event.preventDefault()
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
        id="insuranceType"
        name="insuranceType"
        className="bonus-select__trigger"
        data-placeholder={value === ''}
        disabled={disabled}
        role="combobox"
        aria-labelledby="insuranceType-label"
        aria-expanded={expanded}
        aria-controls="insuranceType-options"
        aria-haspopup="listbox"
        aria-required="true"
        aria-invalid={invalid || undefined}
        aria-describedby={invalid ? 'insuranceType-error' : undefined}
        aria-activedescendant={
          expanded ? `insuranceType-option-${active}` : undefined
        }
        onClick={() => {
          if (expanded) setOpen(false)
          else show()
        }}
      >
        {options.find(([type]) => type === value)?.[1] ??
          'Velg forsikringstype'}
      </button>
      <div className="bonus-select__dropdown" hidden={!expanded}>
        <div
          id="insuranceType-options"
          role="listbox"
          aria-labelledby="insuranceType-label"
        >
          {options.map(([type, label], index) => (
            <div
              key={type}
              id={`insuranceType-option-${index}`}
              role="option"
              tabIndex={-1}
              aria-selected={value === type}
              className={`bonus-select__option${active === index ? ' bonus-select__option--active' : ''}`}
              onClick={() => choose(type)}
            >
              <span>{label}</span>
              {value === type && <span aria-hidden="true">✓</span>}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
