import { test } from 'node:test'
import assert from 'node:assert/strict'
import {
  purchaseInsurance,
  toPurchaseRequest,
} from '../src/api/insurancePurchases.ts'
import { validateField } from '../src/components/insurancePurchaseValidation.ts'

const values = {
  registrationNumber: ' ab 12345 ',
  bonus: '0',
  insuranceType: 'KASKO',
  nationalIdentityNumber: '01019012345',
  firstName: ' Ola ',
  lastName: 'Nordmann',
  email: 'ola@example.no',
}

test('gyldige skjemaverdier konverteres til riktig request', () => {
  const request = toPurchaseRequest(values)

  assert.deepEqual(request, {
    ...values,
    registrationNumber: 'AB12345',
    bonus: 0,
    firstName: 'Ola',
  })
})

test('kjøp sender ett POST-kall og leser SENT-responsen', async (t) => {
  let calls = 0

  t.mock.method(globalThis, 'fetch', async (url: string, init: RequestInit) => {
    calls++

    assert.equal(url, 'https://insurance.example/api/insurance-purchases')
    assert.equal(JSON.parse(String(init.body)).insuranceType, 'KASKO')
    assert.equal(init.method, 'POST')
    assert.equal(
      (init.headers as Record<string, string>)['Content-Type'],
      'application/json',
    )

    return Response.json({
      agreementNumber: 'AVT-123',
      status: 'SENT',
    })
  })

  const response = await purchaseInsurance(values, 'https://insurance.example/')

  assert.deepEqual(response, {
    agreementNumber: 'AVT-123',
    status: 'SENT',
  })

  assert.equal(calls, 1)
})

test('validering avviser ugyldige skjemaverdier', () => {
  assert.ok(validateField('registrationNumber', 'A123'))
  assert.ok(validateField('nationalIdentityNumber', '0101901234'))
  assert.ok(validateField('email', 'invalid'))

  assert.equal(validateField('bonus', '0'), undefined)
  assert.equal(validateField('bonus', '80'), undefined)
  assert.ok(validateField('bonus', '-1'))
  assert.ok(validateField('bonus', '81'))
})

test('forsikringstype kreves og alle fire valg sendes uendret', () => {
  for (const insuranceType of ['ANSVAR', 'DELKASKO', 'KASKO', 'TOPPKASKO']) {
    assert.equal(validateField('insuranceType', insuranceType), undefined)
    assert.equal(
      toPurchaseRequest({ ...values, insuranceType }).insuranceType,
      insuranceType,
    )
  }
  for (const insuranceType of ['', 'UNKNOWN'])
    assert.ok(validateField('insuranceType', insuranceType))
})
