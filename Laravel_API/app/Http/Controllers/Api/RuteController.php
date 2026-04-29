<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Bus;
use App\Models\Halte;
use App\Models\Rute;

class RuteController extends Controller
{
    // Radius dalam meter: bus dianggap "di halte" jika jaraknya <= nilai ini
    private const RADIUS_HALTE_METER = 80;

    public function show(string $busName)
    {
        $bus = Bus::where('nama_bus', strtoupper($busName))->first();

        if (!$bus) {
            return response()->json(['success' => false, 'message' => 'Bus tidak ditemukan'], 404);
        }

        $rute     = Rute::where('bus_id', $bus->bus_id)->first();
        $busLat   = $bus->latitude;
        $busLng   = $bus->longitude;
        $busAktif = $bus->status && $busLat !== null && $busLng !== null;

        // Tentukan rute awal berdasarkan nama bus:
        // Bus A/B → mulai dari Kampus 1 (urutan ASC = halte ke halte ke 4)
        // Bus C/D → mulai dari Kampus 4 (urutan ASC = halte ke halte ke 1)
        $isKampus1Start = in_array(
            strtoupper(trim($busName)),
            ['BUS A', 'BUS B']
        );

        $halte = Halte::where('bus_id', $bus->bus_id)
                      ->orderBy('urutan_halte')
                      ->get();

        $totalHalte          = $halte->count();
        $halteSekarangUrutan = 0;
        $halteSekarangId     = null;
        $jarakKeBusMin       = PHP_FLOAT_MAX;

        if ($busAktif && $totalHalte > 0) {
            foreach ($halte as $h) {
                if ($h->latitude === null || $h->longitude === null) continue;
                $jarak = $this->hitungJarak($busLat, $busLng, $h->latitude, $h->longitude);
                if ($jarak < $jarakKeBusMin) {
                    $jarakKeBusMin       = $jarak;
                    $halteSekarangUrutan = $h->urutan_halte;
                    $halteSekarangId     = $h->halte_id;
                }
            }

            // Jika bus tidak dalam radius 80m dari halte manapun,
            // gunakan halte terdekat tetap sebagai "menuju"
            // (halteSekarangUrutan sudah terisi dengan yang terdekat)
        }

        // Hitung sisa halte dan estimasi
        // Estimasi: ambil dari Google Maps approx — K1↔K4 ~30 menit, tiap halte ~3-4 menit
        $sisaHalte     = max(0, $totalHalte - $halteSekarangUrutan);
        $estimasiMenit = $busAktif ? ($sisaHalte * 3) : 0;

        // Jarak total rute: K1→K4 ≈ 12 km (dari data DataRuteBus)
        $jarakKm = $rute?->jarak_total ?? '12.0 km';

        // Build data halte dengan status
        $halteData = $halte->map(function ($h) use (
            $halteSekarangUrutan, $busAktif, $jarakKeBusMin
        ) {
            if (!$busAktif) {
                $status = 'tidak_aktif';
            } elseif ($h->urutan_halte < $halteSekarangUrutan) {
                $status = 'sudah_lewat';
            } elseif ($h->urutan_halte === $halteSekarangUrutan
                      && $jarakKeBusMin <= self::RADIUS_HALTE_METER) {
                // Bus benar-benar dalam radius halte ini
                $status = 'sekarang';
            } elseif ($h->urutan_halte === $halteSekarangUrutan) {
                // Bus paling dekat ke halte ini tapi belum dalam radius
                $status = 'menuju';
            } else {
                $status = 'menuju';
            }

            return [
                'id'         => $h->halte_id,
                'urutan'     => $h->urutan_halte,
                'nama_halte' => $h->nama_halte,
                'latitude'   => $h->latitude,
                'longitude'  => $h->longitude,
                'status'     => $status,
            ];
        });

        return response()->json([
            'success'          => true,
            'bus'              => $bus->nama_bus,
            'bus_aktif'        => $busAktif,
            'bus_latitude'     => $busLat,
            'bus_longitude'    => $busLng,
            'estimasi'         => $busAktif
                                  ? "$estimasiMenit Menit"
                                  : ($rute?->estimasi_waktu ?? '± 40 Menit'),
            'jarak'            => $jarakKm,
            'jumlah_halte'     => $totalHalte,
            'sisa_halte'       => $sisaHalte,
            'halte_sekarang'   => $halteSekarangUrutan,
            'halte_sekarang_id'=> $halteSekarangId,
            'jarak_ke_halte_m' => round($jarakKeBusMin === PHP_FLOAT_MAX ? 0 : $jarakKeBusMin),
            'halte'            => $halteData,
        ]);
    }

    private function hitungJarak(float $lat1, float $lng1, float $lat2, float $lng2): float
    {
        $R    = 6371000;
        $dLat = deg2rad($lat2 - $lat1);
        $dLng = deg2rad($lng2 - $lng1);
        $a    = sin($dLat / 2) ** 2
              + cos(deg2rad($lat1)) * cos(deg2rad($lat2)) * sin($dLng / 2) ** 2;
        return $R * 2 * atan2(sqrt($a), sqrt(1 - $a));
    }
}