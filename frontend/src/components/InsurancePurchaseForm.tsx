import {
  purchaseInsurance,
  PurchaseApiError,
  type FieldErrors,
  type PurchaseResult,
} from '../api/insurancePurchases'
import {
  useEffect,
  useRef,
  useState,
  type ChangeEvent,
  type SubmitEvent,
} from 'react'
import {
  emptyValues,
  validateField,
  type PurchaseField,
  type PurchaseValues,
} from './insurancePurchaseValidation'
import './InsurancePurchaseForm.css'
import BonusSelect from './BonusSelect'
import InsuranceTypeSelect from './InsuranceTypeSelect'

function InsurancePurchaseForm() {
  const [values, setValues] = useState<PurchaseValues>(emptyValues)
  const [touched, setTouched] = useState<
    Partial<Record<PurchaseField, boolean>>
  >({})

  const [serverErrors, setServerErrors] = useState<FieldErrors>({})
  const [apiError, setApiError] = useState<string>()
  const [result, setResult] = useState<PurchaseResult>()
  const [submitting, setSubmitting] = useState(false)
  const inFlight = useRef(false)
  const pendingErrorFocus = useRef<HTMLElement | null>(null)

  useEffect(() => {
    if (!submitting) {

      pendingErrorFocus.current?.focus()
      pendingErrorFocus.current = null
    }
  }, [submitting])

  function fieldProps(field: PurchaseField) {
    const error =
      serverErrors[field] ??
      (touched[field] ? validateField(field, values[field]) : undefined)
    return {
      value: values[field],
      required: true,
      'aria-invalid': error ? true : undefined,
      'aria-describedby': error ? `${field}-error` : undefined,
      onBlur: () => setTouched((current) => ({ ...current, [field]: true })),
      onChange: (event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const value = event.currentTarget.value
        setValues((current) => ({ ...current, [field]: value }))
        setServerErrors((current) => ({ ...current, [field]: undefined }))
      },
    }
  }

  function fieldError(field: PurchaseField) {
    const message =
      serverErrors[field] ??
      (touched[field] ? validateField(field, values[field]) : undefined)
    return (
      <p className="purchase-form__error" id={`${field}-error`}>
        {message}
      </p>
    )
  }

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    if (inFlight.current || result) return
    const form = event.currentTarget
    setApiError(undefined)
    setServerErrors({})
    const fields = Object.keys(emptyValues) as PurchaseField[]
    setTouched(Object.fromEntries(fields.map((field) => [field, true])))
    const firstInvalid = fields.find((field) =>
      validateField(field, values[field]),
    )
    if (firstInvalid) {
      const control = event.currentTarget.elements.namedItem(firstInvalid)
      if (control instanceof HTMLElement) control.focus()
      return
    }
    inFlight.current = true
    setSubmitting(true)
    try {
      const purchased = await purchaseInsurance(
        values,
        import.meta.env.VITE_API_URL ?? '',
      )
      setResult(purchased)
      setValues(emptyValues)
      setTouched({})
    } catch (error) {
      if (error instanceof PurchaseApiError) {
        setApiError(error.message)
        setServerErrors(error.fieldErrors)
        const field = fields.find((name) => error.fieldErrors[name])
        const control = field ? form.elements.namedItem(field) : null
        pendingErrorFocus.current =
          control instanceof HTMLElement ? control : null
      } else {
        setApiError(
          'Vi kunne ikke bekrefte kjøpet. Kontakt oss før du sender inn på nytt.',
        )
      }
    } finally {
      inFlight.current = false
      setSubmitting(false)
    }
  }

  function handleReset() {
    if (inFlight.current) return
    setServerErrors({})
    setApiError(undefined)
    setValues(emptyValues)
    setTouched({})
  }

  if (result) {
    return (
      <section role="status" className="purchase-form__confirmation">
        <h2>
          <span className="purchase-form__check-circle" aria-hidden="true">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path
                className="purchase-form__check-path"
                d="M4 12l5 5L20 6"
                pathLength="1"
                stroke="currentColor"
                strokeWidth="3"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </span>
          Avtalen er sendt
        </h2>
        <p>Avtalenummer: {result.agreementNumber}</p>
      </section>
    )
  }

  return (
    <form
      className="purchase-form"
      noValidate
      onSubmit={handleSubmit}
      onReset={handleReset}
    >
      <fieldset disabled={submitting} aria-busy={submitting}>
        <legend className="purchase-form__group-label">
          Opplysninger om forsikringen, bilen og deg
        </legend>
        <div className="purchase-form__fields">
          <div className="purchase-form__field">
            <label htmlFor="insuranceType" id="insuranceType-label">
              Forsikringstype
            </label>
            <InsuranceTypeSelect
              value={values.insuranceType}
              disabled={submitting}
              invalid={fieldProps('insuranceType')['aria-invalid']}
              onBlur={fieldProps('insuranceType').onBlur}
              onChange={(insuranceType) => {
                setValues((current) => ({ ...current, insuranceType }))
                setServerErrors((current) => ({
                  ...current,
                  insuranceType: undefined,
                }))
              }}
            />
            {fieldError('insuranceType')}
          </div>
          <div className="purchase-form__field">
            <label htmlFor="registrationNumber">Registreringsnummer</label>
            <input
              {...fieldProps('registrationNumber')}
              id="registrationNumber"
              name="registrationNumber"
              type="text"
              placeholder="F.eks. AB 12345"
              autoCapitalize="characters"
              spellCheck={false}
            />
            {fieldError('registrationNumber')}
          </div>
          <div className="purchase-form__field">
            <label htmlFor="bonus" id="bonus-label">
              Din bonus
            </label>
            <BonusSelect
              value={values.bonus}
              disabled={submitting}
              invalid={fieldProps('bonus')['aria-invalid']}
              onBlur={fieldProps('bonus').onBlur}
              onChange={(bonus) => {
                setValues((current) => ({ ...current, bonus }))
                setServerErrors((current) => ({ ...current, bonus: undefined }))
              }}
            />
            {fieldError('bonus')}
          </div>
          <div className="purchase-form__field">
            <label htmlFor="nationalIdentityNumber">Fødselsnummer</label>
            <input
              {...fieldProps('nationalIdentityNumber')}
              id="nationalIdentityNumber"
              name="nationalIdentityNumber"
              type="text"
              placeholder="11 siffer"
              inputMode="numeric"
              autoComplete="off"
              spellCheck={false}
            />
            {fieldError('nationalIdentityNumber')}
          </div>
          <div className="purchase-form__field">
            <label htmlFor="firstName">Fornavn</label>
            <input
              {...fieldProps('firstName')}
              id="firstName"
              name="firstName"
              type="text"
              autoComplete="given-name"
            />
            {fieldError('firstName')}
          </div>
          <div className="purchase-form__field">
            <label htmlFor="lastName">Etternavn</label>
            <input
              {...fieldProps('lastName')}
              id="lastName"
              name="lastName"
              type="text"
              autoComplete="family-name"
            />
            {fieldError('lastName')}
          </div>
          <div className="purchase-form__field purchase-form__field--full-width">
            <label htmlFor="email">E-post</label>
            <input
              {...fieldProps('email')}
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              autoCapitalize="none"
              spellCheck={false}
            />
            {fieldError('email')}
          </div>
        </div>
      </fieldset>

      {apiError && (
        <p className="purchase-form__error" role="alert">
          {apiError}
        </p>
      )}
      <div
        className="purchase-form__pending"
        role="status"
        aria-live="polite"
        aria-atomic="true"
      >
        {submitting && (
          <>
            <span className="purchase-form__spinner" aria-hidden="true" />
            Sender inn kjøpet …
          </>
        )}
      </div>
      <div className="purchase-form__actions">
        <button
          className="purchase-form__buy"
          type="submit"
          disabled={submitting}
        >
          {submitting ? 'Sender inn …' : 'Kjøp'}
        </button>
        <button
          className="purchase-form__cancel"
          type="reset"
          disabled={submitting}
        >
          Avbryt
        </button>
      </div>
    </form>
  )
}

export default InsurancePurchaseForm
