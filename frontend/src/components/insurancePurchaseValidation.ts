export const insuranceTypes = {
  ANSVAR: 'Ansvar',
  DELKASKO: 'Delkasko',
  KASKO: 'Kasko',
  TOPPKASKO: 'Toppkasko',
} as const

export const emptyValues = {
  insuranceType: '',
  registrationNumber: '',
  bonus: '',
  nationalIdentityNumber: '',
  firstName: '',
  lastName: '',
  email: '',
}

export type PurchaseField = keyof typeof emptyValues
export type PurchaseValues = typeof emptyValues

const requiredMessages: Record<PurchaseField, string> = {
  registrationNumber: 'Bilens registreringsnummer må fylles ut',
  bonus: 'Bonus må velges',
  insuranceType: 'Forsikringstype må velges',
  nationalIdentityNumber: 'Fødselsnummer må fylles ut',
  firstName: 'Fornavn må fylles ut',
  lastName: 'Etternavn må fylles ut',
  email: 'E-post må fylles ut',
}

export function validateField(
  field: PurchaseField,
  value: string,
): string | undefined {
  if (!value.trim()) return requiredMessages[field]

  switch (field) {
    case 'registrationNumber':

    if (!/^[A-Z]{2}[0-9]{5}$/.test(value.replace(/\s/g, '').toUpperCase())) {
        return 'Bruk to bokstaver og fem sifre, for eksempel AB12345.'
      }
      break
    case 'insuranceType':
      if (!Object.hasOwn(insuranceTypes, value))
        return 'Velg en gyldig forsikringstype.'
      break
    case 'bonus':
      if (!/^(?:[0-9]|[1-7][0-9]|80)$/.test(value)) {
        return 'Velg en bonus mellom 0 og 80 %.'
      }
      break
    case 'nationalIdentityNumber':
      if (!/^[0-9]{11}$/.test(value))
        return 'Fødselsnummeret må bestå av nøyaktig 11 sifre.'
      break
    case 'email':
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value))
        return 'Skriv inn en gyldig e-postadresse.'
      break
  }
}
