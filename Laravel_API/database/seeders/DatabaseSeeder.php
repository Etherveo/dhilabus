<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        // Koordinat halte nyata kampus UNG Gorontalo
        $dataRute = [
            'A' => [
                'rute'  => ['nama' => 'Kampus 1 → Kampus 4', 'jarak' => '10.1 KM', 'estimasi' => '27 Menit'],
                'halte' => [
                    ['nama' => 'Gerbang Belakang (A)',      'urutan' => 1, 'lat' => 0.5598,  'lng' => 123.0612],
                    ['nama' => 'Fakultas MIPA & Teknik',    'urutan' => 2, 'lat' => 0.5605,  'lng' => 123.0635],
                    ['nama' => 'Fakultas Pertanian & FSB',  'urutan' => 3, 'lat' => 0.5618,  'lng' => 123.0658],
                    ['nama' => 'Halte Kampus 4',            'urutan' => 4, 'lat' => 0.5630,  'lng' => 123.0710],
                    ['nama' => 'Rektorat UNG (B)',          'urutan' => 5, 'lat' => 0.5642,  'lng' => 123.0748],
                ],
            ],
            'B' => [
                'rute'  => ['nama' => 'Kampus 4 → Kampus 1', 'jarak' => '10.1 KM', 'estimasi' => '27 Menit'],
                'halte' => [
                    ['nama' => 'Rektorat UNG (B)',          'urutan' => 1, 'lat' => 0.5642,  'lng' => 123.0748],
                    ['nama' => 'Halte Kampus 4',            'urutan' => 2, 'lat' => 0.5630,  'lng' => 123.0710],
                    ['nama' => 'Fakultas Pertanian & FSB',  'urutan' => 3, 'lat' => 0.5618,  'lng' => 123.0658],
                    ['nama' => 'Fakultas MIPA & Teknik',    'urutan' => 4, 'lat' => 0.5605,  'lng' => 123.0635],
                    ['nama' => 'Gerbang Belakang (A)',      'urutan' => 5, 'lat' => 0.5598,  'lng' => 123.0612],
                ],
            ],
            'C' => [
                'rute'  => ['nama' => 'Rute C', 'jarak' => '8.5 KM', 'estimasi' => '22 Menit'],
                'halte' => [
                    ['nama' => 'Gerbang Belakang (A)',      'urutan' => 1, 'lat' => 0.5598,  'lng' => 123.0612],
                    ['nama' => 'Fakultas MIPA & Teknik',    'urutan' => 2, 'lat' => 0.5605,  'lng' => 123.0635],
                    ['nama' => 'Halte Kampus 4',            'urutan' => 3, 'lat' => 0.5630,  'lng' => 123.0710],
                    ['nama' => 'Rektorat UNG (B)',          'urutan' => 4, 'lat' => 0.5642,  'lng' => 123.0748],
                ],
            ],
            'D' => [
                'rute'  => ['nama' => 'Rute D', 'jarak' => '8.5 KM', 'estimasi' => '22 Menit'],
                'halte' => [
                    ['nama' => 'Rektorat UNG (B)',          'urutan' => 1, 'lat' => 0.5642,  'lng' => 123.0748],
                    ['nama' => 'Halte Kampus 4',            'urutan' => 2, 'lat' => 0.5630,  'lng' => 123.0710],
                    ['nama' => 'Fakultas MIPA & Teknik',    'urutan' => 3, 'lat' => 0.5605,  'lng' => 123.0635],
                    ['nama' => 'Gerbang Belakang (A)',      'urutan' => 4, 'lat' => 0.5598,  'lng' => 123.0612],
                ],
            ],
        ];

        foreach ($dataRute as $busKey => $data) {
            $user = User::create([
                'name'        => "Pakbus Bus $busKey",
                'nim'         => null,
                'kode_pakbus' => "PKB-$busKey",
                'noTelp'      => null,
                'password'    => Hash::make("pakbus$busKey"),
                'role'        => 'pakbus',
            ]);

            $bus = \App\Models\Bus::create([
                'nama_bus'       => "BUS $busKey",
                'plat_bus'       => "DB 000$busKey",
                'kapasitas'      => 40,
                'status'         => false,
                'latitude'       => null,
                'longitude'      => null,
                'pakbus_user_id' => $user->user_id,
            ]);

            \App\Models\Rute::create([
                'nama_rute'      => $data['rute']['nama'],
                'jarak_total'    => $data['rute']['jarak'],
                'estimasi_waktu' => $data['rute']['estimasi'],
                'bus_id'         => $bus->bus_id,
            ]);

            foreach ($data['halte'] as $h) {
                \App\Models\Halte::create([
                    'bus_id'       => $bus->bus_id,
                    'nama_halte'   => $h['nama'],
                    'urutan_halte' => $h['urutan'],
                    'latitude'     => $h['lat'],
                    'longitude'    => $h['lng'],
                ]);
            }
        }
    }
}