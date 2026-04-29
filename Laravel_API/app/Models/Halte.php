<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Halte extends Model
{
    use HasFactory;

    protected $table = 'halte';
    protected $primaryKey = 'halte_id'; 

    protected $fillable = [
        'bus_id',
        'nama_halte',
        'urutan_halte',
        'latitude',
        'longitude',
    ];

    public function bus()
    {
        return $this->belongsTo(Bus::class, 'bus_id', 'bus_id');
    }
}
