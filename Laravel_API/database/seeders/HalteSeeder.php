<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\Halte;
use App\Models\Bus;

class HalteSeeder extends Seeder
{
    public function run(): void
    {
        // Data halte Kampus 1 → Kampus 4 (untuk Bus A & Bus B)
        $halteK1toK4 = [
            ['nama_halte' => 'Rektorat UNG',       'urutan_halte' => 1, 'latitude' => -0.555721, 'longitude' => 123.063714],
            ['nama_halte' => 'FIP',                'urutan_halte' => 2, 'latitude' => -0.553127, 'longitude' => 123.063306],
            ['nama_halte' => 'Gerbang Kampus 1',   'urutan_halte' => 3, 'latitude' => -0.552663, 'longitude' => 123.063258],
            ['nama_halte' => 'Gerbang Kampus 4',   'urutan_halte' => 4, 'latitude' => -0.553079, 'longitude' => 123.137034],
            ['nama_halte' => 'Perpustakaan',       'urutan_halte' => 5, 'latitude' => -0.554993, 'longitude' => 123.136385],
            ['nama_halte' => 'FSB',                'urutan_halte' => 6, 'latitude' => -0.555330, 'longitude' => 123.137566],
            ['nama_halte' => 'FAPERTA',            'urutan_halte' => 7, 'latitude' => -0.556135, 'longitude' => 123.137477],
            ['nama_halte' => 'Fakultas Teknik',    'urutan_halte' => 8, 'latitude' => -0.555818, 'longitude' => 123.133717],
            ['nama_halte' => 'FMIPA',              'urutan_halte' => 9, 'latitude' => -0.557175, 'longitude' => 123.133913],
        ];

        // Data halte Kampus 4 → Kampus 1 (untuk Bus C & Bus D — urutan dibalik)
        $halteK4toK1 = [
            ['nama_halte' => 'FMIPA',              'urutan_halte' => 1, 'latitude' => -0.557175, 'longitude' => 123.133913],
            ['nama_halte' => 'Fakultas Teknik',    'urutan_halte' => 2, 'latitude' => -0.555818, 'longitude' => 123.133717],
            ['nama_halte' => 'FAPERTA',            'urutan_halte' => 3, 'latitude' => -0.556135, 'longitude' => 123.137477],
            ['nama_halte' => 'FSB',                'urutan_halte' => 4, 'latitude' => -0.555330, 'longitude' => 123.137566],
            ['nama_halte' => 'Perpustakaan',       'urutan_halte' => 5, 'latitude' => -0.554993, 'longitude' => 123.136385],
            ['nama_halte' => 'Gerbang Kampus 4',   'urutan_halte' => 6, 'latitude' => -0.553079, 'longitude' => 123.137034],
            ['nama_halte' => 'Gerbang Kampus 1',   'urutan_halte' => 7, 'latitude' => -0.552663, 'longitude' => 123.063258],
            ['nama_halte' => 'FIP',                'urutan_halte' => 8, 'latitude' => -0.553127, 'longitude' => 123.063306],
            ['nama_halte' => 'Rektorat UNG',       'urutan_halte' => 9, 'latitude' => -0.555721, 'longitude' => 123.063714],
        ];

        // Mapping: nama_bus => data halte
        $mapping = [
            'BUS A' => $halteK1toK4,
            'BUS B' => $halteK1toK4,
            'BUS C' => $halteK4toK1,
            'BUS D' => $halteK4toK1,
        ];

        foreach ($mapping as $namaBus => $halteList) {
            $bus = Bus::where('nama_bus', $namaBus)->first();
            if (!$bus) continue;

            // Hapus halte lama untuk bus ini sebelum insert ulang
            Halte::where('bus_id', $bus->bus_id)->delete();

            foreach ($halteList as $h) {
                Halte::create([
                    'bus_id'       => $bus->bus_id,
                    'nama_halte'   => $h['nama_halte'],
                    'urutan_halte' => $h['urutan_halte'],
                    'latitude'     => $h['latitude'],
                    'longitude'    => $h['longitude'],
                ]);
            }
        }
    }
}