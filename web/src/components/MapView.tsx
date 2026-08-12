import { useEffect, useMemo, useRef } from 'react';
import { MapContainer, Marker, Popup, TileLayer, useMap, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import type { GeoPosition, TreeSummary } from '@/types/tree';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

// Leaflet's default marker icon paths break under bundlers because they're
// resolved at runtime relative to the page, not the bundle. Re-point them at
// the bundled asset URLs once, globally, so every default marker renders correctly.
const defaultIcon = L.icon({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});
L.Marker.prototype.options.icon = defaultIcon;

const userIcon = L.divIcon({
  className: 'user-location-marker',
  html: '<span class="user-location-marker__dot"></span>',
  iconSize: [18, 18],
  iconAnchor: [9, 9],
});

const selectedTreeIcon = L.icon({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
  iconSize: [32, 52],
  iconAnchor: [16, 52],
  popupAnchor: [1, -44],
  shadowSize: [52, 52],
  className: 'tree-marker--selected',
});

interface MapViewProps {
  center: GeoPosition;
  userPosition: GeoPosition | null;
  trees: TreeSummary[];
  selectedTreeId: string | null;
  onSelectTree: (treeId: string) => void;
  onMapMoved: (center: GeoPosition) => void;
}

/** Recenters the map imperatively whenever the `center` prop changes identity (e.g. "recenter" button). */
function MapRecenter({ center }: { center: GeoPosition }) {
  const map = useMap();
  useEffect(() => {
    map.setView([center.latitude, center.longitude], map.getZoom());
    // Only re-run when the center reference changes on purpose (recenter action),
    // not on every render, to avoid fighting the user's own pan/zoom.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [center]);
  return null;
}

/** Reports the map center back to the parent after the user finishes panning/zooming. */
function MapMoveListener({ onMoved }: { onMoved: (center: GeoPosition) => void }) {
  useMapEvents({
    moveend: (event) => {
      const mapCenter = event.target.getCenter();
      onMoved({ latitude: mapCenter.lat, longitude: mapCenter.lng });
    },
  });
  return null;
}

export function MapView({
  center,
  userPosition,
  trees,
  selectedTreeId,
  onSelectTree,
  onMapMoved,
}: MapViewProps) {
  // Keep a stable initial center so remounts (e.g. React strict mode) don't reset the view.
  const initialCenter = useRef(center);
  const markers = useMemo(() => trees, [trees]);

  return (
    <MapContainer
      center={[initialCenter.current.latitude, initialCenter.current.longitude]}
      zoom={17}
      minZoom={12}
      maxZoom={19}
      scrollWheelZoom
      className="map-view"
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        maxZoom={19}
      />
      <MapMoveListener onMoved={onMapMoved} />
      <MapRecenter center={center} />

      {userPosition && (
        <Marker position={[userPosition.latitude, userPosition.longitude]} icon={userIcon}>
          <Popup>You are here</Popup>
        </Marker>
      )}

      {markers.map((tree) => (
        <Marker
          key={tree.treeId}
          position={[tree.latitude, tree.longitude]}
          icon={tree.treeId === selectedTreeId ? selectedTreeIcon : defaultIcon}
          eventHandlers={{
            click: () => onSelectTree(tree.treeId),
          }}
        />
      ))}
    </MapContainer>
  );
}
