<!-- Saaliplaan. Esialgne disain tehtud Claude Codega, seejärel muudetud oma äranägemise järgi -->

<template>
  <div class="hall-plan-wrapper">
    <svg
      class="hall-svg"
      viewBox="0 0 900 685"
      xmlns="http://www.w3.org/2000/svg"
      role="img"
      aria-label="Restaurant hall plan"
    >
      <!-- ── Zone backgrounds ─────────────────────────────────────────────── -->
      <rect x="10" y="10" width="545" height="420" rx="6" class="zone inside-zone" />
      <text x="282" y="32" class="zone-label">Sisesaal</text>

      <rect x="570" y="10" width="320" height="420" rx="6" class="zone terrace-zone" />
      <text x="730" y="32" class="zone-label">Terrass</text>

      <rect x="10" y="445" width="880" height="230" rx="6" class="zone private-zone" />
      <text x="450" y="467" class="zone-label">Privaatruum</text>

      <!-- ── Legend ──────────────────────────────────────────────────────── -->
      <g transform="translate(14, 655)">
        <rect width="14" height="14" rx="3" fill="#94a3b8" />
        <text x="18" y="11" class="legend-text">No search</text>
        <rect x="100" width="14" height="14" rx="3" fill="#4ade80" />
        <text x="118" y="11" class="legend-text">Available</text>
        <rect x="215" width="14" height="14" rx="3" fill="#f87171" />
        <text x="233" y="11" class="legend-text">Occupied</text>
        <rect x="320" width="14" height="14" rx="3" fill="#facc15" />
        <text x="338" y="11" class="legend-text">Top pick</text>
        <rect x="420" width="14" height="14" rx="3" fill="#60a5fa" />
        <text x="438" y="11" class="legend-text">Selected</text>
        <rect x="520" width="14" height="14" rx="3" fill="#d1d5db" />
        <text x="538" y="11" class="legend-text">Filtered out</text>
      </g>

      <!-- ── Lauad ──────────────────────────────────────────────────────── -->
      <g v-for="laud in lauad" :key="laud.id">
        <!-- Circle laud (Terrace) -->
        <template v-if="laud.shape === 'circle'">
          <circle
            :cx="laud.posX"
            :cy="laud.posY"
            :r="laud.width"
            :fill="laudColor(laud)"
            :stroke="laud.id === selectedId ? '#1d4ed8' : '#6b7280'"
            :stroke-width="laud.id === selectedId ? 3 : 1.5"
            class="laud-shape"
            :class="{ clickable: isClickable(laud), selected: laud.id === selectedId }"
            @click="handleClick(laud)"
          />
          <text
            :x="laud.posX"
            :y="laud.posY - 4"
            class="laud-num"
          >{{ laud.lauaNumber }}</text>
          <text
            :x="laud.posX"
            :y="laud.posY + 11"
            class="laud-cap"
          >{{ laud.mahutavus }}p</text>
        </template>

        <!-- Rectangular laud -->
        <template v-else>
          <rect
            :x="laud.posX"
            :y="laud.posY"
            :width="laud.width"
            :height="laud.height"
            rx="4"
            :fill="laudColor(laud)"
            :stroke="laud.id === selectedId ? '#1d4ed8' : '#6b7280'"
            :stroke-width="laud.id === selectedId ? 3 : 1.5"
            class="laud-shape"
            :class="{ clickable: isClickable(laud), selected: laud.id === selectedId }"
            @click="handleClick(laud)"
          />
          <text
            :x="laud.posX + laud.width / 2"
            :y="laud.posY + laud.height / 2 - 5"
            class="laud-num"
          >{{ laud.lauaNumber }}</text>
          <text
            :x="laud.posX + laud.width / 2"
            :y="laud.posY + laud.height / 2 + 10"
            class="laud-cap"
          >{{ laud.mahutavus }}p</text>
        </template>

        <!-- Accessibility icon -->
        <text
          v-if="laud.lastenurk"
          :x="laud.shape === 'circle' ? laud.posX + laud.width - 4 : laud.posX + laud.width - 10"
          :y="laud.shape === 'circle' ? laud.posY - laud.height + 4 : laud.posY + 12"
          class="icon-text"
          title="Lastenurk"
        >🕹️</text>

        <!-- Window icon -->
        <text
          v-if="laud.aknaAll"
          :x="laud.shape === 'circle' ? laud.posX - laud.width + 4 : laud.posX + 2"
          :y="laud.shape === 'circle' ? laud.posY - laud.height + 4 : laud.posY + 12"
          class="icon-text"
          title="Window view"
        >🪟</text>
      </g>
    </svg>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  lauad: {
    type: Array,
    default: () => [],
  },
  recommendationMap: {
    type: Object,
    default: () => ({}),
  },
  selectedId: {
    type: Number,
    default: null,
  },
  searched: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['select'])

const topSkoor = computed(() => {
  const skoorid = Object.values(props.recommendationMap)
    .filter((r) => r.vaba && r.meetsFilter)
    .map((r) => r.skoor)
  return skoorid.length ? Math.max(...skoorid) : 0
})

function laudColor(laud) {
  if (laud.id === props.selectedId) return '#60a5fa' // blue — selected

  if (!props.searched) return '#94a3b8' // gray — no search yet

  const rec = props.recommendationMap[laud.id]
  if (!rec) return '#94a3b8'

  if (!rec.vaba) return '#f87171'        // red — occupied
  if (!rec.meetsFilter) return '#d1d5db' // light gray — filtered out

  // Available and matches: best gets yellow, others green
  if (rec.skoor === topSkoor.value) return '#facc15' // yellow — top pick
  return '#4ade80'                                    // green — available
}

function isClickable(laud) {
  if (!props.searched) return false
  const rec = props.recommendationMap[laud.id]
  return rec && rec.vaba && rec.meetsFilter
}

function handleClick(laud) {
  if (!isClickable(laud)) return
  emit('select', laud.id === props.selectedId ? null : laud.id)
}
</script>

<style scoped>
.hall-plan-wrapper {
  background: #f8f4ef;
  border: 1px solid #e2d8cc;
  border-radius: 8px;
  overflow: hidden;
  padding: 8px;
}

.hall-svg {
  width: 100%;
  height: auto;
  display: block;
}

.zone {
  fill-opacity: 0.35;
  stroke-width: 1.5;
}

.inside-zone  { fill: #fde68a; stroke: #d97706; }
.terrace-zone { fill: #a7f3d0; stroke: #059669; }
.private-zone { fill: #ddd6fe; stroke: #7c3aed; }

.zone-label {
  font-size: 13px;
  font-weight: 700;
  fill: #374151;
  text-anchor: middle;
  dominant-baseline: middle;
}

.laud-shape {
  transition: filter 0.15s;
}

.laud-shape.clickable {
  cursor: pointer;
}

.laud-shape.clickable:hover {
  filter: brightness(0.88);
}

.laud-shape.selected {
  filter: drop-shadow(0 0 4px #3b82f6);
}

.laud-num {
  font-size: 10px;
  font-weight: 700;
  fill: #1f2937;
  text-anchor: middle;
  dominant-baseline: middle;
  pointer-events: none;
}

.laud-cap {
  font-size: 9px;
  fill: #374151;
  text-anchor: middle;
  dominant-baseline: middle;
  pointer-events: none;
}

.icon-text {
  font-size: 8px;
  pointer-events: none;
}

.legend-text {
  font-size: 10px;
  fill: #374151;
  dominant-baseline: middle;
}
</style>
