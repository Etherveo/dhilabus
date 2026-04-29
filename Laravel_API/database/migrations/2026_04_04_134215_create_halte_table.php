<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('halte', function (Blueprint $table) {
            $table->id('halte_id');
            $table->foreignId('bus_id')->constrained('bus', 'bus_id')->onDelete('cascade');
            $table->string('nama_halte');
            $table->integer('urutan_halte');
            $table->decimal('latitude', 10, 7)->nullable();
            $table->decimal('longitude', 11, 7)->nullable();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('halte');
    }
};