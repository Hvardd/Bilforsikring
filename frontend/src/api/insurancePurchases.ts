import type {
  PurchaseField,
  PurchaseValues,
} from '../components/insurancePurchaseValidation'

export type PurchaseResult = { agreementNumber: string; status: 'SENT' }
export type FieldErrors = Partial<Record<PurchaseField, string>>

const fieldMessages: Record<PurchaseField, string> = {
  registrationNumber: 'Kontroller registreringsnummeret.',
  bonus: 'Kontroller valgt bonus.',
  insuranceType: 'Kontroller valgt forsikringstype.',
  nationalIdentityNumber: 'Kontroller fødselsnummeret.',
  firstName: 'Kontroller fornavnet.',
  lastName: 'Kontroller etternavnet.',
  email: 'Kontroller e-postadressen.',
}
const uncertainMessage =
  'Vi kunne ikke bekrefte kjøpet. Kontakt oss før du sender inn på nytt.'

export class PurchaseApiError extends Error {
  fieldErrors: FieldErrors
  constructor(message: string, fieldErrors: FieldErrors = {}) {
    super(message)
    this.fieldErrors = fieldErrors
  }
}

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

export function toPurchaseRequest(values: PurchaseValues) {
  return {
    registrationNumber: values.registrationNumber
      .replace(/\s/g, '')
      .toUpperCase(),
    bonus: Number(values.bonus),
    insuranceType: values.insuranceType,
    nationalIdentityNumber: values.nationalIdentityNumber,
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim(),
    email: values.email.trim(),
  }
}

export async function purchaseInsurance(
  values: PurchaseValues,
  apiUrl: string,
): Promise<PurchaseResult> {
  if (!apiUrl.trim())
    throw new PurchaseApiError(
      'Kjøpstjenesten er ikke tilgjengelig akkurat nå.',
    )
  let response: Response
  try {
    response = await fetch(
      `${apiUrl.trim().replace(/\/+$/, '')}/api/insurance-purchases`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify(toPurchaseRequest(values)),
      },
    )
  } catch {
    throw new PurchaseApiError(uncertainMessage)
  }

  const body: unknown = await response.json().catch(() => null)
  if (!response.ok) {
    if (
      response.status === 400 &&
      isObject(body) &&
      body.code === 'VALIDATION_ERROR'
    ) {
      const fields: FieldErrors = {}
      if (isObject(body.fieldErrors)) {
        for (const field of Object.keys(fieldMessages) as PurchaseField[]) {
          if (Object.hasOwn(body.fieldErrors, field))
            fields[field] = fieldMessages[field]
        }
      }
      throw new PurchaseApiError(
        'Kontroller opplysningene og prøv igjen.',
        fields,
      )
    }
    throw new PurchaseApiError(uncertainMessage)
  }
  if (
    !isObject(body) ||
    body.status !== 'SENT' ||
    typeof body.agreementNumber !== 'string' ||
    !body.agreementNumber.trim()
  ) {
    throw new PurchaseApiError(uncertainMessage)
  }
  return { agreementNumber: body.agreementNumber, status: 'SENT' }
}
