<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Bus extends Model
{
    use HasFactory;

    
    protected $table = 'bus';
    protected $primaryKey = 'bus_id';

    
    protected $fillable = [
        'nama_bus',
        'plat_bus',
        'kapasitas',
        'live_location_url',
        'status',
        'pakbus_user_id',
        'longitude',   
        'latitude',
    ];
}