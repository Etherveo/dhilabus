<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('jadwal', function (Blueprint $table) {
            $table->id('jadwal_id');
            $table->foreignId('rute_id')->constrained('rute', 'rute_id')->onDelete('cascade');
            $table->foreignId('bus_id')->constrained('bus', 'bus_id')->onDelete('cascade');
            $table->time('waktu_berangkat');
            $table->string('titik_awal', 100);
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('jadwal');
    }
};
