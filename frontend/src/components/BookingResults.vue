<!-- Soovitatud laudade osa. Esialgne disain tehtud Claude Codega, seejärel muudetud oma äranägemise järgi -->

<template>
  <div class="results">
    <div v-if="!searched" class="empty-state">
      <span class="empty-icon">🍽️</span>
      <p>Kasuta filtreid, et saaksime soovitada laudu.</p>
    </div>

    <template v-else>
      <h3 class="results-title">
        {{ available.length }} laud{{ available.length !== 1 ? 'a' : '' }} vaba
      </h3>

      <div v-if="available.length === 0 && kombineeritudSoovitused.length === 0" class="empty-state">
        <span class="empty-icon">😔</span>
        <p>Ükski laud ei vasta sinu kriteeriumitele. Muuda filtreid.</p>
      </div>

      <ul v-else class="table-list">
        <li
          v-for="(rec, idx) in available"
          :key="rec.id"
          class="table-card"
          :class="{ 'is-selected': rec.id === selectedId, 'is-top': idx === 0 }"
          @click="emit('select', rec.id === selectedId ? null : rec.id)"
        >
          <div class="card-header">
            <span class="laua-num">{{ rec.lauaNumber }}</span>
            <span v-if="idx === 0" class="badge badge-top">⭐ Parim laud</span>
            <span class="badge badge-score">Skoor {{ rec.skoor }}</span>
          </div>

          <div class="card-body">
            <span class="tag">{{ tsoonLabel(rec.tsoon) }}</span>
            <span class="tag">Kuni {{ rec.mahutavus }} külalist</span>
            <span v-if="rec.aknaAll" class="tag">Akna all</span>
            <span v-if="rec.lastenurk" class="tag">Laste mängunurga lähedal</span>
          </div>

          <button
            class="btn-book"
            @click.stop="emit('book', rec)"
          >
            Broneeri see laud
          </button>
        </li>
      </ul>

      <template v-if="kombineeritudSoovitused.length > 0">
        <h3 class="results-title" style="margin-top: 16px;">Kombineeritud laudade soovitused</h3>
        <ul class="table-list">
          <li
            v-for="pair in kombineeritudSoovitused"
            :key="`${pair.laud1.id}-${pair.laud2.id}`"
            class="table-card"
          >
            <div class="card-header">
              <span class="laua-num">{{ pair.laud1.lauaNumber }} + {{ pair.laud2.lauaNumber }}</span>
              <span class="badge badge-score">Skoor {{ pair.skoor }}</span>
            </div>
            <div class="card-body">
              <span class="tag">{{ tsoonLabel(pair.laud1.tsoon) }}</span>
              <span class="tag">Kuni {{ pair.kombineeritudMahutavus }} külalist</span>
              <span v-if="pair.laud1.aknaAll || pair.laud2.aknaAll" class="tag">Akna all</span>
              <span v-if="pair.laud1.lastenurk || pair.laud2.lastenurk" class="tag">Laste mängunurga lähedal</span>
            </div>
            <button class="btn-book" @click="emit('book-combined', pair)">Broneeri mõlemad lauad</button>
          </li>
        </ul>
      </template>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  soovitused: { type: Array, default: () => [] },
  kombineeritudSoovitused: { type: Array, default: () => [] },
  selectedId: { type: Number, default: null },
  searched: { type: Boolean, default: false },
})

const emit = defineEmits(['select', 'book', 'book-combined'])

const available = computed(() =>
  props.soovitused.filter((r) => r.vaba && r.meetsFilter),
)

const TSOON_LABELS = {
  SISESAAL: 'Sisesaal',
  TERRASS: 'Terrass',
  PRIVAATRUUM: 'Privaatne ruum',
}

function tsoonLabel(tsoon) {
  return TSOON_LABELS[tsoon] ?? tsoon
}
</script>

<style scoped>
.results {
  flex: 1;
  overflow-y: auto;
}

.results-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #374151;
  margin: 0 0 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px 16px;
  color: #9ca3af;
  text-align: center;
}

.empty-icon {
  font-size: 2.5rem;
}

.table-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.table-card {
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s;
  background: #fff;
}

.table-card:hover {
  border-color: #d97706;
  box-shadow: 0 2px 8px rgba(217, 119, 6, 0.1);
}

.table-card.is-selected {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.table-card.is-top {
  border-color: #f59e0b;
  background: #fffbeb;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.laua-num {
  font-size: 1rem;
  font-weight: 700;
  color: #1f2937;
}

.badge {
  font-size: 0.7rem;
  padding: 2px 7px;
  border-radius: 20px;
  font-weight: 600;
}

.badge-top {
  background: #fef3c7;
  color: #92400e;
}

.badge-score {
  margin-left: auto;
  background: #f3f4f6;
  color: #374151;
}

.card-body {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 10px;
}

.tag {
  font-size: 0.75rem;
  padding: 3px 8px;
  background: #f3f4f6;
  border-radius: 12px;
  color: #374151;
}

.btn-book {
  width: 100%;
  padding: 7px;
  background: #d97706;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-book:hover {
  background: #b45309;
}
</style>
