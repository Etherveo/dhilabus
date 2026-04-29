<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Bus;
use Illuminate\Http\Request;

class BusController extends Controller
{
 // Tambahkan method ini di BusController
// Endpoint untuk mahasiswa ambil semua posisi bus yang aktif

public function semuaPosisi()
{
    $buses = Bus::select('bus_id', 'nama_bus', 'latitude', 'longitude', 'status')
        ->get()
        ->map(function ($bus) {
            return [
                'bus_id'    => $bus->bus_id,
                'nama_bus'  => $bus->nama_bus,      
                'latitude'  => $bus->latitude,
                'longitude' => $bus->longitude,
                'is_online' => $bus->status == true
                               && $bus->latitude !== null
                               && $bus->longitude !== null,
            ];
        });

    return response()->json(['success' => true, 'buses' => $buses]);

}

public function posisiPerHalte()
{
    $buses = Bus::with(['halte' => function($query) {
        $query->orderBy('urutan');
    }])
    ->select('bus_id', 'nama_bus', 'latitude', 'longitude', 'status')
    ->get()
    ->map(function ($bus) {
        // Cari posisi halte sekarang dari tabel posisi_bus
        $posisi = \App\Models\PosisiBus::where('bus_id', $bus->bus_id)->first();
        
        return [
            'bus_id'          => $bus->bus_id,
            'nama_bus'        => $bus->nama_bus,
            'latitude'        => $bus->latitude,
            'longitude'       => $bus->longitude,
            'is_online'       => $bus->status == true 
                                 && $bus->latitude !== null,
            'halte_sekarang'  => $posisi?->halte_id_sekarang,
            'kondisi'         => $posisi?->kondisi ?? 'Lancar',
        ];
    });

    return response()->json(['success' => true, 'buses' => $buses]);
}
}
