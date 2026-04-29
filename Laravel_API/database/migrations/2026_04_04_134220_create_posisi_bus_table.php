// posisi_bus migration — lengkap dan benar
<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('posisi_bus', function (Blueprint $table) {
            $table->id();
            $table->foreignId('bus_id')
                  ->constrained('bus', 'bus_id')
                  ->onDelete('cascade');
            $table->foreignId('halte_id_sekarang')
                  ->constrained('halte', 'halte_id')
                  ->onDelete('cascade');
            $table->enum('status_halte', ['menuju', 'tiba', 'berangkat'])
                  ->default('menuju');
            $table->enum('kondisi', ['Lancar', 'Macet', 'Terlambat'])
                  ->default('Lancar');
            $table->timestamp('updated_at')
                  ->useCurrent()
                  ->useCurrentOnUpdate();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('posisi_bus'); // ← bukan riwayat_perjalanan
    }
};