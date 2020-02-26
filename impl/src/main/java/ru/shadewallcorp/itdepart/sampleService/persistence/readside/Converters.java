package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
class Converters {

    /**
     * Конвертер классов данных из персистентной сущности (write-side) в классы для записи в read-side БД.
     */
    static class CassandraToJpa {
        /**
         * Конвертировать заказ persistence entity в заказ JPA.
         *
         * @param source заказ
         * @return заказ JPA.
         */
        @NotNull
        static Order toOrder(@NotNull ru.shadewallcorp.itdepart.sampleService.persistence.entity.Order source) {
            return Order.builder()
                    .id(source.getId())
                    .creationDate(source.getCreationDate() != null ? toCreationDate(source.getCreationDate()) : null)
                    .state(source.getState())
                    .type(source.getType())
                    .siteId(source.getSiteId())
                    .totalPrice(source.getTotalPrice())
                    .payment(source.getPayment() != null ? toPayment(source.getPayment()) : null)
                    .comment(source.getComment())
                    .mnp(source.getMnp())
                    .items(toOrderComposition(source.getItems()))
                    .promo(source.getPromo())
                    .parentNetwork(source.getParentNetwork())
                    .network(source.getNetwork())
                    .partner(source.getPartner())
                    .cancelReason(source.getCancelReason())
                    .delivery(source.getDelivery() != null ? toDelivery(source.getDelivery()) : null)
                    .client(source.getClient() != null ? toClient(source.getClient()) : null)
                    .build();
        }

        /**
         * Получить время в таймзоне сервера и сдвиг таймзоны изначальной даты по сравнению с таймзоной сервера.
         * @param source время с датой и таймзоной
         * @return локальная дата и сдвиг таймзоны
         */
        private static OrderDateTime toCreationDate(ZonedDateTime source) {
            return ofNullable(source).map(dt -> new OrderDateTime(source.toLocalDateTime(), source.getOffset()))
                    .orElse(null);
        }

        /**
         * Копировать список объектов.
         *
         * @param source исходный список (@Nullable)
         * @return копия
         */
        @NotNull
        private static List<OrderCompositionItem> toOrderComposition(
                @Nullable PSequence<ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderCompositionItem>
                        source) {
            return ofNullable(source)
                    .map(Collection::stream)
                    .map(stream -> stream.map(CassandraToJpa::toOrderCompositionItem).collect(toList()))
                    .map(TreePVector::from)
                    .orElse(TreePVector.empty());
        }

        @NotNull
        private static OrderCompositionItem toOrderCompositionItem(
                ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderCompositionItem source) {
            return OrderCompositionItem.builder()
                    .item(source.getItem() != null ? toSkuItem(source.getItem()) : null)
                    .subItem(source.getSubItem() != null ? toSkuSubItem(source.getSubItem()) : null)
                    .itemState(source.getItemState())
                    .build();
        }

        @NotNull
        private static SkuItem toSkuItem(ru.shadewallcorp.itdepart.sampleService.persistence.entity.SkuItem source) {
            return SkuItem.builder()
                    .commerceId(source.getCommerceId())
                    .catalogId(source.getCatalogId() != null ? toCatalogId(source.getCatalogId()) : null)
                    .type(source.getType())
                    .frontName(source.getFrontName())
                    .mnp(source.getMnp())
                    .amount(source.getAmount())
                    .price(source.getPrice())
                    .salePrice(source.getSalePrice())
                    .build();
        }

        @NotNull
        private static SkuSubItem toSkuSubItem(ru.shadewallcorp.itdepart.sampleService.persistence.entity.SkuSubItem
                                                               source) {
            return SkuSubItem.builder()
                    .commerceId(source.getCommerceId())
                    .catalogId(source.getCatalogId() != null ? toCatalogId(source.getCatalogId()) : null)
                    .type(source.getType())
                    .frontName(source.getFrontName())
                    .amount(source.getAmount())
                    .price(source.getPrice())
                    .salePrice(source.getSalePrice())
                    .build();
        }

        @NotNull
        private static CatalogId toCatalogId(ru.shadewallcorp.itdepart.sampleService.persistence.entity.CatalogId
                                                             source) {
            return CatalogId.builder()
                    .productId(source.getProductId())
                    .skuId(source.getSkuId())
                    .serialId(source.getSerialId())
                    .build();
        }

        @NotNull
        private static Payment toPayment(ru.shadewallcorp.itdepart.sampleService.persistence.entity.Payment source) {
            return Payment.builder()
                    .type(source.getType())
                    .state(source.getState())
                    .build();
        }

        @NotNull
        private static Delivery toDelivery(ru.shadewallcorp.itdepart.sampleService.persistence.entity.Delivery source) {
            return Delivery.builder()
                    .deliveryId(source.getId())
                    .name(source.getName())
                    .alias(source.getAlias())
                    .shipGroupType(source.getShipGroupType())
                    .cityId(source.getCityId())
                    .trackNumber(source.getTrackNumber())
                    .tariffZone(source.getTariffZone() != null ? toTariffZone(source.getTariffZone()) : null)
                    .salePoint(source.getSalePoint() != null ? toSalePoint(source.getSalePoint()) : null)
                    .courierInfo(source.getCourierInfo() != null ? toAddress(source.getCourierInfo()) : null)
                    .build();
        }

        @NotNull
        private static TariffZone toTariffZone(ru.shadewallcorp.itdepart.sampleService.persistence.entity.TariffZone
                                                               source) {
            return TariffZone.builder()
                    .id(source.getId())
                    .name(source.getName())
                    .deliveryTimeDesc(source.getDeliveryTimeDesc())
                    .cost(source.getCost())
                    .build();
        }

        @NotNull
        private static SalePoint toSalePoint(ru.shadewallcorp.itdepart.sampleService.persistence.entity.SalePoint
                                                             source) {
            return SalePoint.builder()
                    .id(source.getId())
                    .address(source.getAddress())
                    .build();
        }

        @NotNull
        private static Address toAddress(ru.shadewallcorp.itdepart.sampleService.persistence.entity.Address source) {
            return Address.builder()
                    .postalCode(source.getPostalCode())
                    .city(source.getCity())
                    .street(source.getStreet())
                    .building(source.getBuilding())
                    .apartment(source.getApartment())
                    .build();
        }

        @NotNull
        private static Client toClient(ru.shadewallcorp.itdepart.sampleService.persistence.entity.Client source) {
            return Client.builder()
                    .name(source.getName())
                    .gender(source.getGender())
                    .phone(source.getPhone())
                    .email(source.getEmail())
                    .identityCard(source.getIdentityCard() != null ? toIdentityCard(source.getIdentityCard()) : null)
                    .address(source.getAddress() != null ? toAddress(source.getAddress()) : null)
                    .build();
        }

        @NotNull
        private static IdentityCard toIdentityCard(
                ru.shadewallcorp.itdepart.sampleService.persistence.entity.IdentityCard source) {
            return IdentityCard.builder()
                    .type(source.getType())
                    .series(source.getSeries())
                    .number(source.getNumber())
                    .issuedBy(source.getIssuedBy())
                    .issuedOn(source.getIssuedOn())
                    .issuedCode(source.getIssuedCode())
                    .birthday(source.getBirthday())
                    .placeOfBirth(source.getPlaceOfBirth())
                    .build();
        }
    }

}
