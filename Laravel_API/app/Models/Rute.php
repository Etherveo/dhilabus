<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Rute extends Model
{
    use HasFactory;

    protected $table = 'rute'; // Pastikan nama tabel sesuai database
    protected $primaryKey = 'rute_id'; // Sesuai migrasi Anda

    protected $fillable = [
        'bus_id',
        'nama_rute',
        'jarak_total',
        'estimasi_waktu',
    ];

    public function bus()
    {
        return $this->belongsTo(Bus::class, 'bus_id', 'bus_id');
    }
}