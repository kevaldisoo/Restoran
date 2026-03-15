const BASE = '/api'

async function request(url, options = {}) {
  const res = await fetch(BASE + url, options)
  if (!res.ok) {
    const msg = await res.text().catch(() => res.statusText)
    throw Object.assign(new Error(msg), { status: res.status })
  }
  const text = await res.text()
  return text ? JSON.parse(text) : null
}

export const api = {
  getLauad() {
    return request('/tables')
  },

  getSoovitused({
    kuupaev,
    algusAeg,
    loppAeg,
    kylalisteArv,
    tsoon,
    eelistabAknaAll,
    eelistabLastenurka,
    eelistabPrivaatsust,
  }) {
    const p = new URLSearchParams()
    p.set('kuupaev', kuupaev)
    p.set('algusAeg', algusAeg + ':00')
    p.set('loppAeg', loppAeg + ':00')
    p.set('kylalised', kylalisteArv)
    if (tsoon) p.set('tsoon', tsoon)
    if (eelistabAknaAll != null) p.set('aknaAll', eelistabAknaAll)
    if (eelistabLastenurka != null) p.set('lastenurk', eelistabLastenurka)
    if (eelistabPrivaatsust != null) p.set('privaatsus', eelistabPrivaatsust)
    return request('/tables/recommendations?' + p.toString())
  },

  getKombineeritudSoovitused({
    kuupaev,
    algusAeg,
    loppAeg,
    kylalisteArv,
    tsoon,
    eelistabAknaAll,
    eelistabLastenurka,
    eelistabPrivaatsust,
  }) {
    const p = new URLSearchParams()
    p.set('kuupaev', kuupaev)
    p.set('algusAeg', algusAeg + ':00')
    p.set('loppAeg', loppAeg + ':00')
    p.set('kylalised', kylalisteArv)
    if (tsoon) p.set('tsoon', tsoon)
    if (eelistabAknaAll != null) p.set('aknaAll', eelistabAknaAll)
    if (eelistabLastenurka != null) p.set('lastenurk', eelistabLastenurka)
    if (eelistabPrivaatsust != null) p.set('privaatsus', eelistabPrivaatsust)
    return request('/tables/combined-recommendations?' + p.toString())
  },

  getBroneeringud() {
    return request('/bookings')
  },

  createBroneering(data) {
    return request('/bookings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
  },

  cancelBooking(id) {
    return request('/bookings/' + id, { method: 'DELETE' })
  },

  updateLauaPositsioon(id, data) {
    return request('/tables/' + id + '/position', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
  },
}
