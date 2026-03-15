<template>
  <div class="page">
    <!-- ── Sidebar ──────────────────────────────────────────────────────── -->
    <aside class="sidebar">
      <BookingFilters
        :loading="tableStore.loading"
        :error="tableStore.error"
        :searched="tableStore.searched"
        @search="onSearch"
        @clear="onClear"
      />

      <div class="divider" />

      <BookingResults
        :soovitused="tableStore.soovitused"
        :kombineeritud-soovitused="tableStore.kombineeritudSoovitused"
        :selected-id="selectedTableId"
        :searched="tableStore.searched"
        @select="selectedTableId = $event"
        @book="openModal"
        @book-combined="openModalKombineeritud"
      />
    </aside>

    <!-- ── Main: hall plan ─────────────────────────────────────────────── -->
    <main class="main">
      <h1 class="page-title">Restaurant Table Booking</h1>

      <div v-if="tableStore.loading" class="loading-bar">Loading…</div>

      <HallPlan
        :lauad="tableStore.lauad"
        :recommendation-map="tableStore.recommendationMap"
        :kombineeritud-soovitused="tableStore.kombineeritudSoovitused"
        :selected-id="selectedTableId"
        :searched="tableStore.searched"
        @select="onHallSelect"
      />

      <p v-if="selectedTable" class="selection-hint">
        Selected: <strong>{{ selectedTable.lauaNumber }}</strong>
        ({{ selectedTable.mahutavus }} guests, {{ zoneLabel(selectedTable.tsoon) }})
        <button class="inline-book-btn" @click="openModal(selectedTable)">Book →</button>
      </p>
    </main>

    <!-- ── Booking modal ───────────────────────────────────────────────── -->
    <BookingModal
      :show="showModal"
      :laud="modalTable"
      :filter="lastFilter"
      :loading="bookingStore.loading"
      :error="bookingStore.error"
      @cancel="showModal = false"
      @confirm="onConfirmBooking"
    />

    <!-- ── Success toast ───────────────────────────────────────────────── -->
    <Transition name="toast">
      <div v-if="toast" class="toast">✅ {{ toast }}</div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import HallPlan from '@/components/HallPlan.vue'
import BookingFilters from '@/components/BookingFilters.vue'
import BookingResults from '@/components/BookingResults.vue'
import BookingModal from '@/components/BookingModal.vue'
import { useTableStore } from '@/stores/tableStore.js'
import { useBookingStore } from '@/stores/bookingStore.js'

const tableStore  = useTableStore()
const bookingStore = useBookingStore()

const selectedTableId = ref(null)
const showModal   = ref(false)
const modalTable  = ref(null)
const modalLaud2  = ref(null)
const lastFilter  = ref(null)
const toast       = ref('')

onMounted(() => tableStore.fetchLauad())

const selectedTable = computed(() => {
  if (!selectedTableId.value) return null
  return tableStore.recommendationMap[selectedTableId.value] ?? null
})

const ZONE_LABELS = { SISESAAL: 'Sisesaal', TERRASS: 'Terrass', PRIVAATRUUM: 'Privaatruum' }
function zoneLabel(zone) { return ZONE_LABELS[zone] ?? zone }

async function onSearch(filter) {
  lastFilter.value = filter
  selectedTableId.value = null
  await tableStore.fetchSoovitused(filter)
  await tableStore.fetchKombineeritudSoovitused(filter)
}

function onClear() {
  tableStore.clearSoovitused()
  selectedTableId.value = null
}

function onHallSelect(id) { selectedTableId.value = id }

function openModal(rec) {
  modalTable.value = rec
  bookingStore.error = null
  showModal.value = true
}

function openModalKombineeritud(pair) {
  modalTable.value = {
    ...pair.laud1,
    lauaNumber: `${pair.laud1.lauaNumber} + ${pair.laud2.lauaNumber}`,
    mahutavus: pair.kombineeritudMahutavus,
  }
  modalLaud2.value = pair.laud2
  bookingStore.error = null
  showModal.value = true
}

async function onConfirmBooking({ kylaline, kommentaar }) {
  if (!modalTable.value || !lastFilter.value) return
  const f = lastFilter.value
  try {
    await bookingStore.createBroneering({
      lauaId:        modalTable.value.id,
      kylastaja:     kylaline,
      kylalisteArv:  f.kylalisteArv,
      algusAeg:      `${f.kuupaev}T${f.algusAeg}:00`,
      loppAeg:       `${f.kuupaev}T${f.loppAeg}:00`,
      kommentaar,
      kombineeritud: !!modalLaud2.value,
    })
    if (modalLaud2.value) {
      await bookingStore.createBroneering({
        lauaId:        modalLaud2.value.id,
        kylastaja:     kylaline,
        kylalisteArv:  f.kylalisteArv,
        algusAeg:      `${f.kuupaev}T${f.algusAeg}:00`,
        loppAeg:       `${f.kuupaev}T${f.loppAeg}:00`,
        kommentaar,
        kombineeritud: true,
      })
    }
    const toastMsg = modalLaud2.value
      ? `Tables ${modalTable.value.lauaNumber} booked for ${kylaline}!`
      : `Table ${modalTable.value.lauaNumber} booked for ${kylaline}!`
    modalLaud2.value = null
    showModal.value = false
    selectedTableId.value = null
    await tableStore.fetchSoovitused(f)
    showToast(toastMsg)
  } catch {
    // error shown inside modal via bookingStore.error
  }
}

function showToast(msg) {
  toast.value = msg
  setTimeout(() => { toast.value = '' }, 4000)
}
</script>

<style scoped>
.page {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 340px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding: 20px 16px;
  background: #faf7f2;
  border-right: 1px solid #e8ddd0;
}

.divider {
  height: 1px;
  background: #e5e7eb;
  margin: 16px 0;
}

.main {
  flex: 1;
  overflow-y: visible;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f9f5f0;
}

.page-title {
  font-size: 1.4rem;
  font-weight: 800;
  color: #78350f;
  margin: 0;
}

.loading-bar {
  font-size: 0.85rem;
  color: #d97706;
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.4; }
}

.selection-hint {
  font-size: 0.9rem;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
}

.inline-book-btn {
  padding: 4px 12px;
  background: #d97706;
  color: #fff;
  border: none;
  border-radius: 5px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
}

.inline-book-btn:hover { background: #b45309; }

.toast {
  position: fixed;
  bottom: 28px;
  right: 28px;
  background: #166534;
  color: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  box-shadow: 0 4px 16px rgba(0,0,0,0.2);
  z-index: 200;
}

.toast-enter-active, .toast-leave-active { transition: opacity .3s, transform .3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateY(12px); }
</style>
