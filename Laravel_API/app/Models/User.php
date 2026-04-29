<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;


class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    protected $table = 'users';     
    protected $primaryKey = 'user_id'; 
    public $incrementing = true;  
    protected $keyType = 'int'; 


    protected $fillable = [
        'name',
        'nim',
        'kode_pakbus',        
        'noTelp',
        'password',
        'role',
    ];

    protected $hidden = [
        'password',
        'remember_token',
    ];

    
}