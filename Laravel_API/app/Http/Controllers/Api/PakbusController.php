<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Bus;
use App\Models\Halte;
use App\Models\PosisiBus;
use Illuminate\Http\Request;

class PakbusController extends Controller
{
    public function updateKoordinat(Request $request)
    {
        $user = $request->user();

        if ($user->role !== 'pakbus') {
            return response()->json(['success' => false, 'message' => 'Akses ditolak'], 403);
        }

        $request->validate([
            'latitude'  => 'required|numeric|between:-90,90',
            'longitude' => 'required|numeric|between:-180,180',
        ]);

        $bus = Bus::where('pakbus_user_id', $user->user_id)->first();

        if (!$bus) {
            return response()->json(['success' => false, 'message' => 'Bus tidak ditemukan'], 404);
        }

        $bus->update([
            'latitude'  => $request->latitude,
            'longitude' => $request->longitude,
        ]);

        return response()->json(['success' => true, 'message' => 'Koordinat diperbarui']);
    }

    public function toggleAktif(Request $request)
    {
        $user = $request->user();

        if ($user->role !== 'pakbus') {
            return response()->json(['success' => false, 'message' => 'Akses ditolak'], 403);
        }

        $request->validate([
            'status' => 'required|boolean',
        ]);

        $bus = Bus::where('pakbus_user_id', $user->user_id)->first();

        if (!$bus) {
            return response()->json(['success' => false, 'message' => 'Bus tidak ditemukan'], 404);
        }

        $bus->update(['status' => $request->status]);

        return response()->json([
            'success' => true,
            'message' => $request->status ? 'Bus aktif' : 'Bus nonaktif',
            'status'  => $bus->status,
        ]);
    }

    public function getBusSaya(Request $request)
    {
        $user = $request->user();

        if ($user->role !== 'pakbus') {
            return response()->json(['success' => false, 'message' => 'Akses ditolak'], 403);
        }

        $bus = Bus::where('pakbus_user_id', $user->user_id)->first();

        if (!$bus) {
            return response()->json(['success' => false, 'message' => 'Bus tidak ditemukan'], 404);
        }

        $halteList = Halte::where('bus_id', $bus->bus_id)->orderBy('urutan')->get();
        $posisi    = PosisiBus::where('bus_id', $bus->bus_id)->first();

        return response()->json([
            'success'        => true,
            'bus'            => $bus->nama_bus,
            'halte'          => $halteList,
            'halte_sekarang' => $posisi?->halte_id_sekarang,
            'kondisi'        => $posisi?->kondisi ?? 'Lancar',
        ]);
    }

    public function updatePosisi(Request $request)
    {
        $user = $request->user();

        if ($user->role !== 'pakbus') {
            return response()->json(['success' => false, 'message' => 'Akses ditolak'], 403);
        }

        $request->validate([
            'halte_id' => 'required|exists:halte,halte_id',
            'kondisi'  => 'required|in:Lancar,Macet,Terlambat',
        ]);

        $bus = Bus::where('pakbus_user_id', $user->user_id)->firstOrFail();

        PosisiBus::updateOrCreate(
            ['bus_id' => $bus->bus_id],
            [
                'halte_id_sekarang' => $request->halte_id,
                'kondisi'           => $request->kondisi,
                'status_halte'      => 'tiba',
            ]
        );

        return response()->json(['success' => true, 'message' => 'Posisi berhasil diperbarui']);
    }
}