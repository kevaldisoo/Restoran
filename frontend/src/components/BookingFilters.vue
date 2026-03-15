<!-- Filtrite osa. Esialgne disain tehtud Claude Codega, seejärel muudetud oma äranägemise järgi -->

<template>
  <form class="filters" @submit.prevent="submit">
    <h2 class="filters-title">Leia laud</h2>

    <div class="field">
      <label for="kuupaev">Kuupäev</label>
      <input id="kuupaev" v-model="form.kuupaev" type="number" :min="today" required />
    </div>

    <div class="field-row">
      <div class="field">
        <label for="algusAeg">Alates</label>
        <input id="algusAeg" v-model="form.algusAeg" type="time" required />
      </div>
      <div class="field">
        <label for="loppAeg">Kuni</label>
        <input id="loppAeg" v-model="form.loppAeg" type="time" required />
      </div>
    </div>

    <div class="field">
      <label for="kylalisteArv">Külaliste arv</label>
      <input
        id="kylalisteArv"
        v-model.number="form.kylalisteArv"
        type="number"
        min="1"
        max="20"
        required
      />
    </div>

    <div class="field">
      <label for="tsoon">Tsoon</label>
      <select id="tsoon" v-model="form.tsoon">
        <option value="">Kõik tsoonid</option>
        <option value="SISESAAL">Sisesaal</option>
        <option value="TERRASS">Terrass</option>
        <option value="PRIVAATRUUM">Privaatruum</option>
      </select>
    </div>

    <fieldset class="pref-group">
      <legend>Eelistused</legend>

      <label class="pref-item">
        <input v-model="form.eelistabAknaAll" type="checkbox" />
        <span>Akna all </span>
      </label>
      <label class="pref-item">
        <input v-model="form.eelistabLastenurka" type="checkbox" />
        <span>Laste mängunurga lähedal</span>
      </label>
      <label class="pref-item">
        <input v-model="form.eelistabPrivaatust" type="checkbox" />
        <span>Privaatsus</span>
      </label>
    </fieldset>

    <p v-if="validationError" class="error-msg">{{ validationError }}</p>
    <p v-else-if="error" class="error-msg">{{ error }}</p>

    <button type="submit" class="btn-primary" :disabled="loading">
      {{ loading ? 'Otsin…' : 'Otsi laudu' }}
    </button>

    <button v-if="searched" type="button" class="btn-secondary" @click="clear">Puhasta</button>
  </form>
</template>

<script setup>
import { reactive, ref } from 'vue'

const emit = defineEmits(['search', 'clear'])

const validationError = ref('')

const today = new Date().toISOString().slice(0, 10)

const form = reactive({
  kuupaev: today,
  algusAeg: '19:00',
  loppAeg: '21:00',
  kylalisteArv: 2,
  tsoon: '',
  eelistabAknaAll: false,
  eelistabLastenurka: false,
  eelistabPrivaatust: false,
})

function submit() {
  validationError.value = ''
  if (form.algusAeg < '11:00' || form.loppAeg > '23:00') {
    validationError.value = 'Restoran on avatud 11:00–23:00'
    return
  }
  emit('search', {
    kuupaev: form.kuupaev,
    algusAeg: form.algusAeg,
    loppAeg: form.loppAeg,
    kylalisteArv: form.kylalisteArv,
    tsoon: form.tsoon || null,
    eelistabAknaAll: form.eelistabAknaAll || null,
    eelistabLastenurka: form.eelistabLastenurka || null,
    eelistabPrivaatust: form.eelistabPrivaatust || null,
  })
}

function clear() {
  form.tsoon = ''
  form.eelistabAknaAll = false
  form.eelistabLastenurka = false
  form.eelistabPrivaatust = false
  emit('clear')
}
</script>

<style scoped>
.filters {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.filters-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #78350f;
  margin: 0;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #4b5563;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

input[type='date'],
input[type='time'],
input[type='number'],
select {
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  background: #fff;
  color: #111827;
  transition: border-color 0.15s;
}

input:focus,
select:focus {
  outline: none;
  border-color: #d97706;
  box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.15);
}

.pref-group {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pref-group legend {
  font-size: 0.8rem;
  font-weight: 600;
  color: #4b5563;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 0 4px;
}

.pref-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  color: #374151;
}

.pref-item input[type='checkbox'] {
  width: 16px;
  height: 16px;
  accent-color: #d97706;
}

.error-msg {
  color: #dc2626;
  font-size: 0.85rem;
  margin: 0;
}

.btn-primary {
  padding: 10px 0;
  background: #d97706;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-primary:hover:not(:disabled) {
  background: #b45309;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 8px 0;
  background: transparent;
  color: #6b7280;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-secondary:hover {
  background: #f3f4f6;
}
</style>
