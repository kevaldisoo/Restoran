<!-- Broneeringu vaheleht. Esialgne disain tehtud Claude Codega, seejärel muudetud oma äranägemise järgi -->
<template>
  <Teleport to="body">
    <div v-if="show" class="overlay" @click.self="emit('cancel')">
      <div class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <button class="close-btn" @click="emit('cancel')" aria-label="Close">✕</button>

        <h2 id="modal-title" class="modal-title">Kinnita broneering</h2>

        <div v-if="laud" class="laua-summary">
          <div class="summary-row">
            <span class="summary-label">Laud</span>
            <span class="summary-value">{{ laud.lauaNumber }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Tsoon</span>
            <span class="summary-value">{{ tsoonLabel(laud.tsoon) }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Mahutavas</span>
            <span class="summary-value">Kuni {{ laud.mahutavus }} külalist</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Kuupäev</span>
            <span class="summary-value">{{ filter?.kuupaev }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Kellaaeg</span>
            <span class="summary-value">{{ filter?.algusAeg }} – {{ filter?.loppAeg }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Külaliste arv</span>
            <span class="summary-value">{{ filter?.kylalisteArv }}</span>
          </div>
        </div>

        <form @submit.prevent="confirm">
          <div class="field">
            <label for="kylaline">Sinu nimi</label>
            <input
              id="kylaline"
              v-model="kylaline"
              type="text"
              placeholder="Sinu nimi"
              required
              autocomplete="name"
            />
          </div>

          <div class="field">
            <label for="kommentaar">Kommentaarid</label>
            <textarea
              id="kommentaar"
              v-model="kommentaar"
              rows="2"
              placeholder="Allergiad, muud nõuded..."
            />
          </div>

          <p v-if="error" class="error-msg">{{ error }}</p>

          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="emit('cancel')">Cancel</button>
            <button type="submit" class="btn-confirm" :disabled="loading">
              {{ loading ? 'Broneerin...' : 'Kinnita broneering' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  laud: Object,
  filter: Object,
  loading: Boolean,
  error: String,
})

const emit = defineEmits(['cancel', 'confirm'])

const kylaline = ref('')
const kommentaar = ref('')

// Reset form each time the modal opens
watch(
  () => props.show,
  (open) => {
    if (open) {
      kylaline.value = ''
      kommentaar.value = ''
    }
  },
)

const TSOON_LABELS = {
  SISESAAL: 'Sisesaal',
  TERRASS: 'Terrass',
  PRIVAATRUUM: 'Privaatruum',
}

function tsoonLabel(tsoon) {
  return TSOON_LABELS[tsoon] ?? tsoon
}

function confirm() {
  emit('confirm', {
    kylaline: kylaline.value,
    kommentaar: kommentaar.value,
  })
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 16px;
}

.modal {
  background: #fff;
  border-radius: 12px;
  padding: 28px;
  width: 100%;
  max-width: 440px;
  position: relative;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.close-btn {
  position: absolute;
  top: 14px;
  right: 14px;
  background: none;
  border: none;
  font-size: 1.1rem;
  cursor: pointer;
  color: #6b7280;
  line-height: 1;
  padding: 4px;
  border-radius: 4px;
}

.close-btn:hover { background: #f3f4f6; }

.modal-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: #78350f;
  margin: 0 0 16px;
}

.laua-summary {
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
}

.summary-label { color: #6b7280; }
.summary-value { font-weight: 600; color: #1f2937; }

.field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.field label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #4b5563;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.field input,
.field textarea {
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  font-family: inherit;
  resize: vertical;
}

.field input:focus,
.field textarea:focus {
  outline: none;
  border-color: #d97706;
  box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.15);
}

.error-msg {
  color: #dc2626;
  font-size: 0.85rem;
  margin: 0 0 12px;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-cancel {
  padding: 9px 18px;
  background: transparent;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  color: #374151;
}

.btn-cancel:hover { background: #f3f4f6; }

.btn-confirm {
  padding: 9px 18px;
  background: #d97706;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-confirm:hover:not(:disabled) { background: #b45309; }
.btn-confirm:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
