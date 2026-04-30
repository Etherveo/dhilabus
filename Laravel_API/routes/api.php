<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\Api\BusController;
use App\Http\Controllers\Api\PakbusController;
use App\Http\Controllers\Api\RuteController;

// Endpoint Publik (Tidak butuh token)
Route::post('/register', [AuthController::class, 'register']);
Route::post('/login',        [AuthController::class, 'login']);       
Route::post('/pakbus/login', [AuthController::class, 'loginPakbus']); 

Route::middleware('auth:sanctum')->group(function () {
    
    Route::middleware('auth:sanctum')->post('/bus/{id}/location', [BusController::class, 'updateLocation']);
    Route::post('/logout', [AuthController::class, 'logout']);
    
    
    Route::get('/users', function (Request $request) {
        return response()->json([
            'success' => true,
            'data' => $request->user()
        ]);
        
    });

    Route::prefix('pakbus')->group(function () {
        Route::get('/bus-saya',       [PakbusController::class, 'getBusSaya']);
        Route::post('/update-posisi', [PakbusController::class, 'updatePosisi']);
        Route::post('/update-koordinat', [PakbusController::class, 'updateKoordinat']);
        Route::post('/toggle-aktif', [PakbusController::class, 'toggleAktif']);
    });

    
    Route::get('/bus/semua-posisi', [BusController::class, 'semuaPosisi']);
    Route::get('/bus/posisi-halte', [BusController::class, 'posisiPerHalte']);
    Route::get('/rute/{busName}', [RuteController::class, 'show']);
});