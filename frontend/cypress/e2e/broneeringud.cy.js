const today = new Date().toISOString().slice(0, 10)

describe('Restorani broneeringud', () => {
  beforeEach(() => {
    cy.visit('/')
    cy.contains('Restorani laudade broneerimine')
  })

  it('Broneerib laua kahele täna kell 20:00–22:00', () => {
    cy.get('#kuupaev').clear().type(today)
    cy.get('#algusAeg').clear().type('20:00')
    cy.get('#loppAeg').clear().type('22:00')
    cy.get('#kylalisteArv').clear().type('2')

    cy.contains('button', 'Otsi laudu').click()

    // Oota kuni tulemused ilmuvad
    cy.contains('laud', { timeout: 8000 })

    cy.contains('button', 'Broneeri see laud').first().click()
    cy.get('#kylaline').type('Test Kasutaja')
    cy.contains('button', 'Kinnita broneering').click()

    cy.contains('Test Kasutaja', { timeout: 8000 })
  })

  it('Broneerib kombineeritud lauad 12 külalisele - leiab esimese vaba aja', () => {
    cy.get('#kylalisteArv').clear().type('12')

    // Intercept kombineeritud soovituste endpoint - kogub kõik vastused järjekorda
    cy.intercept('GET', '/api/tables/combined-recommendations*').as('combinedRecs')

    // Genereerib ajavahemikud: iga 2 tunni tagant kella 10–20, järgmised 7 päeva.
    const slots = []
    for (let day = 0; day < 7; day++) {
      const d = new Date()
      d.setDate(d.getDate() + day)
      const dateStr = d.toISOString().slice(0, 10)
      for (let hour = 10; hour <= 20; hour += 2) {
        slots.push({
          kuupaev: dateStr,
          algus: String(hour).padStart(2, '0') + ':00',
          lopp: String(hour + 2).padStart(2, '0') + ':00',
        })
      }
    }

    /**
     * Funktsioon, mis otsib, kas leidub vaba kombineeritud laudu. Kui ei, siis otsib uut ajavahemikku. Selle funktsiooniga
     * on abi saadud Claude Code poolt.
     */
    function trySlot(idx) {
      if (idx >= slots.length) {
        throw new Error('Ei leitud ühtegi vaba kombineeritud lauda lähema 14 päeva jooksul')
      }

      const slot = slots[idx]
      cy.get('#kuupaev').clear().type(slot.kuupaev)
      cy.get('#algusAeg').clear().type(slot.algus)
      cy.get('#loppAeg').clear().type(slot.lopp)
      cy.contains('button', 'Otsi laudu').click()

      // Oota kuni see konkreetne päring vastab
      cy.wait('@combinedRecs', { timeout: 10000 }).then(({ response }) => {
        if (response.body && response.body.length > 0) {
          // Leitud — broneeri esimene kombineeritud paar
          cy.contains('button', 'Broneeri mõlemad lauad').first().click()
          cy.get('#kylaline').type('Suur Grupp')
          cy.contains('button', 'Kinnita broneering').click()
          cy.contains('Suur Grupp', { timeout: 8000 })
        } else {
          // Tühi vastus — proovi järgmist ajavahemikku
          trySlot(idx + 1)
        }
      })
    }

    trySlot(0)
  })
})
